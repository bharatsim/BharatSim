package com.bharatsim.engine.distributed.actors

import akka.{Done, MockAdapterMsg}
import akka.actor.CoordinatedShutdown
import akka.actor.CoordinatedShutdown.Reason
import akka.actor.testkit.typed.Effect
import akka.actor.testkit.typed.Effect.{MessageAdapter, Spawned}
import akka.actor.testkit.typed.scaladsl.{ActorTestKit, BehaviorTestKit}
import akka.actor.typed.scaladsl.ActorContext
import com.bharatsim.engine.Context
import com.bharatsim.engine.distributed.DBBookmark
import com.bharatsim.engine.distributed.Guardian.UserInitiatedShutdown
import com.bharatsim.engine.distributed.actors.Barrier.WorkFinished
import com.bharatsim.engine.distributed.actors.DistributedTickLoop.{
  ExecuteWrites,
  WORK_BARRIER,
  WRITE_BARRIER,
  WriteFinished
}
import com.bharatsim.engine.execution.actions._
import com.bharatsim.engine.graph.GraphProviderFactory
import com.bharatsim.engine.graph.neo4j.BatchNeo4jProvider
import org.mockito.{ArgumentMatchersSugar, MockitoSugar}
import org.scalatest.BeforeAndAfterEach
import org.scalatest.funspec.AnyFunSpec
import org.scalatest.matchers.should.Matchers

import scala.concurrent.duration.Duration
import scala.concurrent.{Await, ExecutionContext, Future}

class DistributedTickLoopTest
    extends AnyFunSpec
    with BeforeAndAfterEach
    with Matchers
    with MockitoSugar
    with ArgumentMatchersSugar {

  val mockGraphProvider = mock[BatchNeo4jProvider]
  val localBookmark = DBBookmark(java.util.Set.of("localBK"))
  GraphProviderFactory.testOverride(mockGraphProvider)
  val workerCoordinator = mock[WorkerCoordinator]

  val context = Context()
  val actions = mockActions()

  val workerCount = 1
  val bookmarks = List(DBBookmark(java.util.Set.of("b1")))
  val tick = 1

  override def beforeEach(): Unit = {
    when(mockGraphProvider.executePendingWrites()).thenReturn(Future.successful(localBookmark))
    when(workerCoordinator.workerCount).thenReturn(workerCount)
  }
  override def afterEach(): Unit = {
    MockitoSugar.reset(mockGraphProvider)
    MockitoSugar.reset(workerCoordinator)

  }
  private def mockActions(): Actions = {
    val actions = mock[Actions]
    val preSim = mock[PreSimulationActions]
    val postSim = mock[PostSimulationActions]
    val preTick = mock[PreTickActions]
    val postTick = mock[PostTickActions]
    when(actions.preSimulation).thenReturn(preSim)
    when(actions.postSimulation).thenReturn(postSim)
    when(actions.preTick).thenReturn(preTick)
    when(actions.postTick).thenReturn(postTick)
    actions
  }

  private def getBarrier(effects: Seq[Effect], name: String, tick: Int = 1) = {
    effects
      .find({
        case Spawned(_, childName, _) => childName == s"${name}-${tick}"
        case _                        => false
      })
  }

  describe("Start Tick") {

    it("should start the work for tick") {
      val testKit = BehaviorTestKit(DistributedTickLoop(context, actions, tick, bookmarks, workerCoordinator))

      val effects = testKit.retrieveAllEffects()

      val barrierWorkBarrier: Spawned[Barrier.Request] =
        getBarrier(effects, WORK_BARRIER).get.asInstanceOf[Spawned[Barrier.Request]]

      verify(mockGraphProvider).setBookmarks(bookmarks)
      verify(actions.preTick).execute(tick)
      verify(workerCoordinator).initTick(
        any[ActorContext[DistributedTickLoop.Command]],
        eqTo(context),
        eqTo(bookmarks)
      )
      verify(workerCoordinator).startWork(
        any[ActorContext[DistributedTickLoop.Command]],
        eqTo(context),
        eqTo(barrierWorkBarrier.ref)
      )

    }

    it("should stop at the end of simulation and terminate actor system") {
      val toolKit = ActorTestKit()
      val lastTick = context.simulationConfig.simulationSteps + 1
      val coordinatedShutdownMonitor = spyLambda((reason: Reason) => "");
      val coordinatedShutdown = CoordinatedShutdown(toolKit.system)
      coordinatedShutdown
        .addTask(CoordinatedShutdown.PhaseBeforeServiceUnbind, "test") { () =>
          Future {

            coordinatedShutdownMonitor(coordinatedShutdown.getShutdownReason().get)
            Done
          }(ExecutionContext.global)
        }

      toolKit.spawn(DistributedTickLoop(context, actions, lastTick, bookmarks, workerCoordinator))

      Await.ready(toolKit.system.whenTerminated, Duration.Inf)

      verify(coordinatedShutdownMonitor)(UserInitiatedShutdown)
      verify(mockGraphProvider).setBookmarks(bookmarks)
      verify(actions.postSimulation).execute()
      verify(workerCoordinator, times(0)).initTick(
        any[ActorContext[DistributedTickLoop.Command]],
        eqTo(context),
        eqTo(bookmarks)
      )
      toolKit.shutdownTestKit()
    }

    it("should execute writes after work is finish") {
      val testKit = BehaviorTestKit(DistributedTickLoop(context, actions, tick, bookmarks, workerCoordinator))

      val effects = testKit.retrieveAllEffects()

      val barrierWorkBarrier: Spawned[Barrier.Request] =
        getBarrier(effects, WORK_BARRIER).get.asInstanceOf[Spawned[Barrier.Request]]

      val barrierAdaptor = effects
        .filter(_.isInstanceOf[MessageAdapter[Barrier.BarrierFinished, DistributedTickLoop.Command]])
        .head
        .asInstanceOf[MessageAdapter[Barrier.BarrierFinished, DistributedTickLoop.Command]]

      testKit.selfInbox().hasMessages shouldBe false

      val barrierTestKit = testKit.childTestKit(barrierWorkBarrier.ref)
      barrierTestKit.run(WorkFinished())
      testKit.selfInbox().hasMessages shouldBe true
      barrierTestKit.isAlive shouldBe false

      val receivedMessage = testKit.selfInbox().receiveAll().head.asInstanceOf[Any]

      val msgToAdapt = MockAdapterMsg[Barrier.BarrierFinished](receivedMessage).getMessage()
      barrierAdaptor.adapt(msgToAdapt) shouldBe ExecuteWrites
    }

  }

  describe("Execute Writes") {

    it("should start executing writes") {
      val testKit = BehaviorTestKit(DistributedTickLoop(context, actions, tick, bookmarks, workerCoordinator))

      testKit.run(ExecuteWrites)
      val effects = testKit.retrieveAllEffects()
      val barrierWriteBarrier: Spawned[Barrier.Request] =
        getBarrier(effects, WRITE_BARRIER).get.asInstanceOf[Spawned[Barrier.Request]]

      verify(workerCoordinator).notifyExecuteWrites(barrierWriteBarrier.ref)
    }

    it("should finish works when all worker are done") {
      val testKit = BehaviorTestKit(DistributedTickLoop(context, actions, tick, bookmarks, workerCoordinator))
      val workerBookmark = DBBookmark(java.util.Set.of("wbk1"))
      testKit.run(ExecuteWrites)
      val effects = testKit.retrieveAllEffects()

      testKit.selfInbox().hasMessages shouldBe false
      val barrierWriteBarrier: Spawned[Barrier.Request] =
        getBarrier(effects, WRITE_BARRIER).get.asInstanceOf[Spawned[Barrier.Request]]
      val barrierTestKit = testKit.childTestKit(barrierWriteBarrier.ref)
      barrierTestKit.runOne()
      barrierTestKit.run(WorkFinished(Some(workerBookmark)))
      testKit.selfInbox().hasMessages shouldBe true
      testKit.selfInbox().hasMessages shouldBe true
      barrierTestKit.isAlive shouldBe false

      val barrierAdaptor = effects
        .filter(_.isInstanceOf[MessageAdapter[Barrier.BarrierFinished, DistributedTickLoop.Command]])
        .last
        .asInstanceOf[MessageAdapter[Barrier.BarrierFinished, DistributedTickLoop.Command]]
      val receivedMessage = testKit.selfInbox().receiveAll().head.asInstanceOf[Any]

      val msgToAdapt = MockAdapterMsg[Barrier.BarrierFinished](receivedMessage).getMessage()
      barrierAdaptor.adapt(msgToAdapt) shouldBe WriteFinished(List(workerBookmark, localBookmark))
    }
  }

  describe("Write Finished") {

    it("should switch to next tick") {
      val testKit = BehaviorTestKit(DistributedTickLoop(context, actions, tick, bookmarks, workerCoordinator))
      val bookmarkAfterTickFinished = List(DBBookmark(java.util.Set.of("Tick1 bookmarks")))

      testKit.run(WriteFinished(bookmarkAfterTickFinished))

      val effects = testKit.retrieveAllEffects()
      val barrierWorkBarrier: Spawned[Barrier.Request] =
        getBarrier(effects, WORK_BARRIER, 2).get.asInstanceOf[Spawned[Barrier.Request]]
      verify(mockGraphProvider).setBookmarks(bookmarkAfterTickFinished)
      verify(actions.preTick).execute(tick + 1)
      verify(workerCoordinator).initTick(
        any[ActorContext[DistributedTickLoop.Command]],
        eqTo(context),
        eqTo(bookmarkAfterTickFinished)
      )
      verify(workerCoordinator).startWork(
        any[ActorContext[DistributedTickLoop.Command]],
        eqTo(context),
        eqTo(barrierWorkBarrier.ref)
      )
    }
  }
}

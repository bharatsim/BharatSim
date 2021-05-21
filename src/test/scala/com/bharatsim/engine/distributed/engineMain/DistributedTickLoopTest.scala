package com.bharatsim.engine.distributed.engineMain

import akka.actor.CoordinatedShutdown
import akka.actor.CoordinatedShutdown.Reason
import akka.actor.testkit.typed.Effect
import akka.actor.testkit.typed.Effect.{MessageAdapter, Spawned}
import akka.actor.testkit.typed.scaladsl.{ActorTestKit, BehaviorTestKit, TestInbox}
import akka.actor.typed.ActorSystem
import akka.{Done, MockAdapterMsg}
import com.bharatsim.engine.Context
import com.bharatsim.engine.distributed.DBBookmark
import com.bharatsim.engine.distributed.Guardian.UserInitiatedShutdown
import com.bharatsim.engine.distributed.engineMain.Barrier.WorkFinished
import com.bharatsim.engine.distributed.engineMain.DistributedTickLoop._
import com.bharatsim.engine.distributed.engineMain.WorkDistributor.{AgentLabelExhausted, FetchWork}
import com.bharatsim.engine.distributed.worker.WorkerActor
import com.bharatsim.engine.execution.actions._
import com.bharatsim.engine.graph.neo4j.BatchNeo4jProvider
import org.mockito.{ArgumentMatchersSugar, InOrder, Mockito, MockitoSugar}
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
  val workerCoordinator = mock[WorkerCoordinator]

  val context = spy(Context(mockGraphProvider))

  val workerCount = 1
  val bookmarks = List(DBBookmark(java.util.Set.of("b1")))
  val tick = 1
  val agentLabels = List("A1")

  override def beforeEach(): Unit = {
    when(mockGraphProvider.executePendingWrites()).thenReturn(Future.successful(localBookmark))
    when(workerCoordinator.workerCount).thenReturn(workerCount)
    when(context.agentLabels).thenReturn(agentLabels)
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

  private def getGetChildActor(effects: Seq[Effect], name: String, tick: Int = 1) = {
    effects
      .find({
        case Spawned(_, childName, _) => childName == s"${name}-${tick}"
        case _                        => false
      })
  }

  describe("Start Tick") {

    it("should start the work for tick") {
      val actions = mockActions()
      val testKit = BehaviorTestKit(DistributedTickLoop(context, actions, tick, bookmarks, workerCoordinator))

      val effects = testKit.retrieveAllEffects()

      val workDistributor: Spawned[WorkDistributor.Command] =
        getGetChildActor(effects, WORK_DISTRIBUTOR).get.asInstanceOf[Spawned[WorkDistributor.Command]]

      val order: InOrder = Mockito.inOrder(actions.preSimulation, actions.preTick)
      order.verify(actions.preSimulation).execute()
      order.verify(actions.preTick).execute(tick)

      verify(mockGraphProvider).setBookmarks(bookmarks)
      verify(workerCoordinator).initTick(
        any[ActorSystem[_]],
        eqTo(context),
        eqTo(bookmarks)
      )
      verify(workerCoordinator).startWork(workDistributor.ref)
    }

    it("should stop at the end of simulation and terminate actor system") {
      val actions = mockActions()
      val testKit = ActorTestKit()
      val lastTick = context.simulationConfig.simulationSteps + 1
      val coordinatedShutdownMonitor = spyLambda((reason: Reason) => "");
      val coordinatedShutdown = CoordinatedShutdown(testKit.system)
      coordinatedShutdown
        .addTask(CoordinatedShutdown.PhaseBeforeServiceUnbind, "test") { () =>
          Future {
            coordinatedShutdownMonitor(coordinatedShutdown.getShutdownReason().get)
            Done
          }(ExecutionContext.global)
        }

      testKit.spawn(DistributedTickLoop(context, actions, lastTick, bookmarks, workerCoordinator))

      Await.ready(testKit.system.whenTerminated, Duration.Inf)

      verify(coordinatedShutdownMonitor)(UserInitiatedShutdown)
      verify(mockGraphProvider).setBookmarks(bookmarks)
      verify(actions.postSimulation).execute()
      verify(workerCoordinator, times(0)).initTick(
        any[ActorSystem[_]],
        eqTo(context),
        eqTo(bookmarks)
      )
      testKit.shutdownTestKit()
    }

    it("should execute writes after work is finish") {
      val actions = mockActions()
      val testKit = BehaviorTestKit(DistributedTickLoop(context, actions, tick, bookmarks, workerCoordinator))

      val worker = TestInbox[WorkerActor.Command]()
      val effects = testKit.retrieveAllEffects()
      val barrierWorkBarrier: Spawned[Barrier.Request] =
        getGetChildActor(effects, WORK_BARRIER).get.asInstanceOf[Spawned[Barrier.Request]]

      val workDistributor: Spawned[WorkDistributor.Command] =
        getGetChildActor(effects, WORK_DISTRIBUTOR).get.asInstanceOf[Spawned[WorkDistributor.Command]]

      val barrierAdaptor = effects
        .filter(_.isInstanceOf[MessageAdapter[Barrier.BarrierFinished, DistributedTickLoop.Command]])
        .head
        .asInstanceOf[MessageAdapter[Barrier.BarrierFinished, DistributedTickLoop.Command]]

      testKit.selfInbox().hasMessages shouldBe false

      val workerTestKit = testKit.childTestKit(workDistributor.ref)
      workerTestKit.run(AgentLabelExhausted(agentLabels.head))
      workerTestKit.run(FetchWork(worker.ref))
      testKit.childTestKit(barrierWorkBarrier.ref).runOne()
      testKit.selfInbox().hasMessages shouldBe true

      val receivedMessage = testKit.selfInbox().receiveAll().head.asInstanceOf[Any]

      val msgToAdapt = MockAdapterMsg[Barrier.BarrierFinished](receivedMessage).getMessage()
      barrierAdaptor.adapt(msgToAdapt) shouldBe ExecuteWrites
    }

    it("should skip work distribution when there are no agent labels") {
      val actions = mockActions()
      val context = spy(Context(mockGraphProvider))
      when(context.agentLabels).thenReturn(List.empty)
      val testKit = BehaviorTestKit(DistributedTickLoop(context, actions, tick, bookmarks, workerCoordinator))

      testKit.hasEffects() shouldBe false
      testKit.selfInbox().receiveMessage() shouldBe ExecuteWrites
    }

  }

  describe("Execute Writes") {

    it("should start executing writes") {
      val actions = mockActions()
      val testKit = BehaviorTestKit(DistributedTickLoop(context, actions, tick, bookmarks, workerCoordinator))

      testKit.run(ExecuteWrites)
      val effects = testKit.retrieveAllEffects()
      val barrierWriteBarrier: Spawned[Barrier.Request] =
        getGetChildActor(effects, WRITE_BARRIER).get.asInstanceOf[Spawned[Barrier.Request]]

      verify(workerCoordinator).executeWrites(barrierWriteBarrier.ref)
    }

    it("should finish works when all worker are done") {
      val actions = mockActions()
      val testKit = BehaviorTestKit(DistributedTickLoop(context, actions, tick, bookmarks, workerCoordinator))
      val workerBookmark = DBBookmark(java.util.Set.of("wbk1"))
      testKit.run(ExecuteWrites)
      val effects = testKit.retrieveAllEffects()

      testKit.selfInbox().hasMessages shouldBe false
      val barrierWriteBarrier: Spawned[Barrier.Request] =
        getGetChildActor(effects, WRITE_BARRIER).get.asInstanceOf[Spawned[Barrier.Request]]
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
      val actions = mockActions()
      val testKit = BehaviorTestKit(DistributedTickLoop(context, actions, tick, bookmarks, workerCoordinator))
      val bookmarkAfterTickFinished = List(DBBookmark(java.util.Set.of("Tick1 bookmarks")))

      testKit.run(WriteFinished(bookmarkAfterTickFinished))

      val effects = testKit.retrieveAllEffects()
      val workDistributor: Spawned[WorkDistributor.Command] =
        getGetChildActor(effects, WORK_DISTRIBUTOR, 2).get.asInstanceOf[Spawned[WorkDistributor.Command]]
      verify(mockGraphProvider).setBookmarks(bookmarkAfterTickFinished)
      verify(actions.preTick).execute(tick + 1)
      verify(workerCoordinator).initTick(
        any[ActorSystem[_]],
        eqTo(context),
        eqTo(bookmarkAfterTickFinished)
      )
      verify(workerCoordinator).startWork(workDistributor.ref)
    }
  }
}

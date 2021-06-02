package com.bharatsim.engine.distributed.engineMain

import akka.actor.testkit.typed.scaladsl.ActorTestKit
import akka.actor.typed.receptionist.Receptionist
import akka.actor.typed.scaladsl.Behaviors
import com.bharatsim.engine.Context
import com.bharatsim.engine.distributed.engineMain.DistributedTickLoop.StartOfNewTickAck
import com.bharatsim.engine.distributed.engineMain.WorkDistributor.Start
import com.bharatsim.engine.distributed.worker.WorkerActor
import com.bharatsim.engine.distributed.worker.WorkerActor.{
  ExecutePendingWrites,
  Shutdown,
  StartOfNewTick,
  workerServiceId
}
import com.bharatsim.engine.distributed.{ContextData, DBBookmark}
import com.bharatsim.engine.graph.neo4j.BatchNeo4jProvider
import org.mockito.MockitoSugar
import org.scalatest.funspec.AnyFunSpec
import org.scalatest.matchers.should.Matchers
import org.scalatest.{BeforeAndAfterAll, BeforeAndAfterEach}

class WorkerCoordinatorTest
    extends AnyFunSpec
    with BeforeAndAfterAll
    with BeforeAndAfterEach
    with Matchers
    with MockitoSugar {
  val testKit = ActorTestKit()

  val workDistributor = testKit.createTestProbe[WorkDistributor.Command]()
  val mockGraphProvider = mock[BatchNeo4jProvider]
  val context = Context(mockGraphProvider)
  val bookmarks = List(DBBookmark(java.util.Set.of("bk")))

  val mockedWorkerBehavior = Behaviors.receiveMessage[WorkerActor.Command] { msg =>
    msg match {
      case StartOfNewTick(_, _, replyTo) => replyTo ! StartOfNewTickAck()
      case _                             =>
    }
    Behaviors.same
  }

  private var workerProbe = testKit.createTestProbe[WorkerActor.Command]()
  private var workerProxy = testKit.spawn(Behaviors.monitor(workerProbe.ref, mockedWorkerBehavior))
  testKit.system.receptionist ! Receptionist.register(workerServiceId, workerProxy)

  override def afterEach(): Unit = {
    workerProbe.stop()
    testKit.stop(workerProxy)
    workerProbe = testKit.createTestProbe[WorkerActor.Command]()
    workerProxy = testKit.spawn(Behaviors.monitor(workerProbe.ref, mockedWorkerBehavior))
    testKit.system.receptionist ! Receptionist.register(workerServiceId, workerProxy)
  }

  override def afterAll() {
    testKit.shutdownTestKit()
  }

  describe("initialise tick") {
    it("should notify start of the tick to all workers") {
      val coordinator = new WorkerCoordinator()
      coordinator.initTick(testKit.system, context, bookmarks)
      val expectedContext = ContextData(context.getCurrentStep, context.activeInterventionNames)
      val startNewTickMsg = workerProbe.receiveMessage().asInstanceOf[StartOfNewTick]
      startNewTickMsg.updatedContext shouldBe expectedContext
      startNewTickMsg.bookmarks shouldBe bookmarks
    }
    it("should give worker count after initialisation") {
      val coordinator = new WorkerCoordinator()
      coordinator.workerCount shouldBe 0
      coordinator.initTick(testKit.system, context, bookmarks)
      coordinator.workerCount shouldBe 1
    }
  }

  describe("start work") {
    it("should start work distribution") {
      val coordinator = new WorkerCoordinator()
      coordinator.initTick(testKit.system, context, bookmarks)

      coordinator.startWork(workDistributor.ref)
      workDistributor.expectMessage(Start(List(workerProxy)))
    }
  }

  describe("execute writes") {
    it("should notify worker for execute writes") {
      val coordinator = new WorkerCoordinator()
      coordinator.initTick(testKit.system, context, bookmarks)
      val barrier = testKit.createTestProbe[Barrier.Request]()
      workerProbe.expectMessageType[StartOfNewTick]
      coordinator.executeWrites(barrier.ref)
      workerProbe.expectMessage(ExecutePendingWrites(barrier.ref))
    }
  }

  describe("trigger shutdown") {
    it("should notify workers to shutdown") {
      val reason = "Test Reason"
      val coordinator = new WorkerCoordinator()
      coordinator.initTick(testKit.system, context, bookmarks)
      val testProb = testKit.createTestProbe()
      workerProbe.expectMessageType[StartOfNewTick]
      coordinator.triggerShutdown(reason, testProb.ref)
      workerProbe.expectMessage(Shutdown(reason, testProb.ref))
    }
  }

}

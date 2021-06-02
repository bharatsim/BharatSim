package com.bharatsim.engine.distributed.engineMain

import akka.actor.testkit.typed.scaladsl.{ActorTestKit, BehaviorTestKit, TestInbox}
import com.bharatsim.engine.distributed.engineMain.Barrier.{NotifyOnBarrierFinished, WorkErrored, WorkFinished}
import com.bharatsim.engine.distributed.engineMain.WorkDistributor.{AgentLabelExhausted, FetchWork, Start, WorkFailed}
import com.bharatsim.engine.distributed.worker.WorkerActor
import com.bharatsim.engine.distributed.worker.WorkerActor.Work
import org.scalatest.BeforeAndAfterAll
import org.scalatest.funspec.AnyFunSpec
import org.scalatest.matchers.should.Matchers

class WorkDistributorTest extends AnyFunSpec with BeforeAndAfterAll with Matchers {
  val testKit = ActorTestKit()

  val barrier = testKit.createTestProbe[Barrier.Request]()
  val worker1 = testKit.createTestProbe[WorkerActor.Command]()
  val worker2 = testKit.createTestProbe[WorkerActor.Command]()
  val batchSize = 1
  val agentLabels = List("A1", "A2")
  val distributableWork = new DistributableWork(agentLabels, batchSize)

  override protected def afterAll(): Unit = {
    testKit.shutdownTestKit()
  }
  describe("Start") {
    it("should distribute work to all available worker") {
      val workDistributor =
        testKit.spawn(WorkDistributor(barrier.ref, distributableWork))
      workDistributor ! Start(List(worker1.ref, worker2.ref))

      worker1.expectMessage(Work(agentLabels.head, 0, batchSize, workDistributor))
      worker2.expectMessage(Work(agentLabels.head, 1, batchSize, workDistributor))
      testKit.stop(workDistributor)
    }
    it("should register with barrier and stop on barrier finished") {
      val barrierInbox = TestInbox[Barrier.Request]()
      val worker = TestInbox[WorkerActor.Command]()
      val workDistributorTestKit =
        BehaviorTestKit(WorkDistributor(barrierInbox.ref, distributableWork))
      workDistributorTestKit.run(Start(List(worker.ref)))
      val message = barrierInbox.receiveMessage().asInstanceOf[NotifyOnBarrierFinished]
      message.toRef ! Barrier.BarrierFinished(List.empty)
      workDistributorTestKit.runOne()
      workDistributorTestKit.isAlive shouldBe false
    }
  }
  describe("FetchWork") {
    it("should send next available work to worker") {
      val workDistributor =
        testKit.spawn(WorkDistributor(barrier.ref, distributableWork))
      workDistributor ! FetchWork(worker1.ref)
      workDistributor ! FetchWork(worker2.ref)

      worker1.expectMessage(Work(agentLabels.head, 0, batchSize, workDistributor))
      worker2.expectMessage(Work(agentLabels.head, 1, batchSize, workDistributor))

      workDistributor ! FetchWork(worker2.ref)
      worker2.expectMessage(Work(agentLabels.head, 2, batchSize, workDistributor))

      testKit.stop(workDistributor)
    }

    it("should notify barrier when all the work is distributed") {
      val emptyDistributableWork = new DistributableWork(List.empty, batchSize)
      val workDistributor =
        testKit.spawn(WorkDistributor(barrier.ref, emptyDistributableWork))
      workDistributor ! FetchWork(worker1.ref)

      barrier.expectMessageType[NotifyOnBarrierFinished]
      barrier.expectMessage(WorkFinished())
      testKit.stop(workDistributor)
    }
  }

  describe("WorkFailed") {
    it("should notify barrier on workFailed") {
      val workDistributor =
        testKit.spawn(WorkDistributor(barrier.ref, distributableWork))
      val reason = "Test Reason"
      workDistributor ! WorkFailed(reason, worker1.ref)
      barrier.expectMessage(WorkErrored(reason, worker1.ref))
      testKit.stop(workDistributor)
    }
  }
  describe("AgentLabelExhausted") {
    it("should switch to the next available label for distribution") {
      val workDistributor =
        testKit.spawn(WorkDistributor(barrier.ref, distributableWork))

      workDistributor ! AgentLabelExhausted(distributableWork.agentLabel)

      workDistributor ! FetchWork(worker1.ref)
      worker1.expectMessage(Work(distributableWork.nextAgentLabel.agentLabel, 0, batchSize, workDistributor))
      testKit.stop(workDistributor)
    }

    it("should not switch label when requested with old label") {
      val alreadyOnNextLabel = distributableWork.nextAgentLabel
      val workDistributor =
        testKit.spawn(WorkDistributor(barrier.ref, alreadyOnNextLabel))

      workDistributor ! AgentLabelExhausted(agentLabels.head)

      workDistributor ! FetchWork(worker1.ref)
      worker1.expectMessage(Work(alreadyOnNextLabel.agentLabel, 0, batchSize, workDistributor))
      testKit.stop(workDistributor)
    }
  }

  describe("Stop") {
    it("should stop the actor") {
      val workDistributor =
        BehaviorTestKit(WorkDistributor(barrier.ref, distributableWork))
      workDistributor.run(WorkDistributor.Stop)
      workDistributor.isAlive shouldBe false
    }
  }
}

package com.bharatsim.engine.distributed

import akka.actor.testkit.typed.scaladsl.BehaviorTestKit
import akka.actor.typed.receptionist.Receptionist
import com.bharatsim.engine.Context
import com.bharatsim.engine.distributed.WorkerManager.workerServiceId
import com.bharatsim.engine.distributed.engineMain.WorkerCoordinator
import com.bharatsim.engine.execution.SimulationDefinition
import com.bharatsim.engine.graph.GraphProviderFactory
import com.bharatsim.engine.graph.neo4j.BatchNeo4jProvider
import org.mockito.Mockito.clearInvocations
import org.mockito.{ArgumentCaptor, ArgumentMatchersSugar, MockitoSugar}
import org.scalatest.BeforeAndAfterEach
import org.scalatest.funspec.AnyFunSpec
import org.scalatest.matchers.should.Matchers

class WorkerManagerTest
    extends AnyFunSpec
    with MockitoSugar
    with BeforeAndAfterEach
    with Matchers
    with ArgumentMatchersSugar {
  val ingestionStep = spyLambda((context: Context) => {})
  val body = spyLambda((context: Context) => {})
  val onComplete = spyLambda((context: Context) => {})
  val mockWorkerCoordinator = mock[WorkerCoordinator]
  val simDef = SimulationDefinition(ingestionStep, body, onComplete)
  val mockGraphProvider = mock[BatchNeo4jProvider]
  GraphProviderFactory.testOverride(mockGraphProvider)
  override def afterEach(): Unit = {
    clearInvocations(ingestionStep)
    clearInvocations(body)
    clearInvocations(onComplete)
    clearInvocations(mockWorkerCoordinator)
  }

  describe("WorkerManager") {
    it("should register self with receptionist") {
      val workerManager = new WorkerManager().start(simDef)
      val workerTestKit = BehaviorTestKit(workerManager)
      workerTestKit.receptionistInbox().expectMessage(Receptionist.register(workerServiceId, workerTestKit.ref))
    }
    it("should only execute simulation body") {
      val workerManager = new WorkerManager().start(simDef)

      BehaviorTestKit(workerManager)

      val contextCaptor = ArgumentCaptor.forClass(classOf[Context])

      verify(ingestionStep, never)(any[Context])
      verify(onComplete, never)(any[Context])
      verify(body)(contextCaptor.capture())
      val context = contextCaptor.getValue.asInstanceOf[Context]
      context.graphProvider shouldBe mockGraphProvider
    }
  }
}

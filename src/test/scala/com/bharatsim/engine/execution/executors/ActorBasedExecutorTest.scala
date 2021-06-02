package com.bharatsim.engine.execution.executors

import akka.Done
import com.bharatsim.engine.Context
import com.bharatsim.engine.execution.SimulationDefinition
import com.bharatsim.engine.execution.actorbased.ActorBackedSimulation
import org.mockito.{ArgumentCaptor, ArgumentMatchersSugar, MockitoSugar}
import org.scalatest.funsuite.AnyFunSuite
import org.scalatest.matchers.should.Matchers

import scala.concurrent.Future

class ActorBasedExecutorTest extends AnyFunSuite with MockitoSugar with Matchers with ArgumentMatchersSugar {

  test("should run actor back simulation") {
    val mockActorBackedSimulation = mock[ActorBackedSimulation]
    when(mockActorBackedSimulation.run(any[Context])).thenReturn(Future.successful(Done))
    def mockFn() = spyLambda((context: Context) => {})
    val simDef = SimulationDefinition(mockFn(), mockFn(), mockFn())
    val agentBasedExecutor = new ActorBasedExecutor(mockActorBackedSimulation)
    agentBasedExecutor.execute(simDef)
    val contextCaptor = ArgumentCaptor.forClass(classOf[Context])
    verify(simDef.ingestionStep)(contextCaptor.capture())
    val context = contextCaptor.getValue.asInstanceOf[Context]
    verify(mockActorBackedSimulation).run(context)
    agentBasedExecutor shouldBe a[DefaultExecutor]
  }
  test("should Escalate the exception") {
    val mockActorBackedSimulation = mock[ActorBackedSimulation]
    val exception = new Exception("TestError")
    when(mockActorBackedSimulation.run(any[Context])).thenReturn(Future.failed(exception))
    def mockFn() = spyLambda((context: Context) => {})
    val simDef = SimulationDefinition(mockFn(), mockFn(), mockFn())
    val agentBasedExecutor = new ActorBasedExecutor(mockActorBackedSimulation)

    val thrown: Exception = the[Exception] thrownBy (agentBasedExecutor.execute(simDef))
    thrown.getMessage shouldBe exception.getMessage

  }
}

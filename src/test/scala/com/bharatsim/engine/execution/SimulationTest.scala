package com.bharatsim.engine.execution

import com.bharatsim.engine.ContextBuilder.{registerAction, registerAgent}
import com.bharatsim.engine.actions.StopSimulation
import com.bharatsim.engine.basicConversions.decoders.DefaultDecoders._
import com.bharatsim.engine.cache.PerTickCache
import com.bharatsim.engine.execution.SimulationTest.graphNodeStudent
import com.bharatsim.engine.execution.control.{BehaviourControl, StateControl}
import com.bharatsim.engine.execution.simulation.{PostSimulationActions, PreSimulationActions}
import com.bharatsim.engine.graph.{GraphNode, GraphProvider}
import com.bharatsim.engine.testModels.Employee.employeeBehaviour
import com.bharatsim.engine.testModels.Student
import com.bharatsim.engine.testModels.Student.{studentBehaviour1, studentBehaviour2}
import com.bharatsim.engine.{ApplicationConfig, Context, Dynamics, SimulationConfig}
import org.mockito.Mockito.clearInvocations
import org.mockito.MockitoSugar
import org.scalatest.BeforeAndAfterEach
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec

class SimulationTest extends AnyWordSpec with MockitoSugar with BeforeAndAfterEach with Matchers {
  override def beforeEach(): Unit = {
    clearInvocations(studentBehaviour1)
    clearInvocations(studentBehaviour2)
    clearInvocations(employeeBehaviour)
  }
  "run" should {
    "execute steps provided in config" in {
      val graphProvider = {
        val gp = mock[GraphProvider]
        when(gp.fetchNodes("Student")).thenReturn(List(graphNodeStudent))
        gp
      }
      val stateControl = mock[StateControl]
      val behaviourControl = mock[BehaviourControl]
      val agentExecutor = new AgentExecutor(behaviourControl, stateControl)
      val steps = 2
      implicit val context: Context = getContext(steps, graphProvider)

      registerAgent[Student]

      new Simulation(context, mock[ApplicationConfig], agentExecutor, mock[PreSimulationActions], mock[PostSimulationActions]).run()

      verify(behaviourControl, times(2)).executeFor(graphNodeStudent.as[Student])
    }

    "stop executing steps" when {
      "end of simulation is marked as true" in {
        val graphProvider = {
          val gp = mock[GraphProvider]
          when(gp.fetchNodes("Student")).thenReturn(List(graphNodeStudent))
          gp
        }
        val stateControl = mock[StateControl]
        val behaviourControl = mock[BehaviourControl]
        val agentExecutor = new AgentExecutor(behaviourControl, stateControl)
        val steps = 5
        implicit val context: Context = getContext(steps, graphProvider)

        registerAgent[Student]
        registerAction(StopSimulation, c => c.getCurrentStep == 3)

        new Simulation(context, mock[ApplicationConfig], agentExecutor, mock[PreSimulationActions], mock[PostSimulationActions]).run()

        verify(behaviourControl, times(3)).executeFor(graphNodeStudent.as[Student])
      }
    }
  }

  private def getContext(
      steps: Int,
      mockGraphProvider: GraphProvider = mock[GraphProvider],
      perTickCache: PerTickCache = mock[PerTickCache]
  ) = {
    new Context(mockGraphProvider, new Dynamics, SimulationConfig(steps), perTickCache)
  }
}

object SimulationTest {
  val graphNodeStudent: GraphNode = GraphNode("Student", 1)

  val graphNodeEmployee: GraphNode = GraphNode("Employee", 2)
}

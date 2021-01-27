package com.bharatsim.engine.execution

import com.bharatsim.engine.basicConversions.decoders.BasicMapDecoder
import com.bharatsim.engine.basicConversions.decoders.DefaultDecoders._
import com.bharatsim.engine.execution.AgentExecutorTest.{
  graphNodeStudent,
  statefulPerson,
  statefulPersonDecoder,
  studentDecoder
}
import com.bharatsim.engine.execution.control.{BehaviourControl, StateControl}
import com.bharatsim.engine.graph.GraphNode
import com.bharatsim.engine.testModels.{StatefulPerson, Student}
import org.mockito.MockitoSugar.{mock, verify}
import org.scalatest.wordspec.AnyWordSpec

class AgentExecutorTest extends AnyWordSpec {
  "exec" should {
    "invoke behaviour control for an agent" in {
      val behaviourControl = mock[BehaviourControl]
      val stateControl = mock[StateControl]

      val step = new AgentExecutor(behaviourControl, stateControl)
      step.execute(NodeWithDecoder(graphNodeStudent, studentDecoder))

      verify(behaviourControl).executeFor(graphNodeStudent.as[Student])
    }

    "invoke state control for an stateful agent" in {
      val behaviourControl = mock[BehaviourControl]
      val stateControl = mock[StateControl]

      val step = new AgentExecutor(behaviourControl, stateControl)
      step.execute(NodeWithDecoder(statefulPerson, statefulPersonDecoder))

      verify(stateControl).executeFor(statefulPerson.as[StatefulPerson])
    }
  }
}

object AgentExecutorTest {
  val graphNodeStudent: GraphNode = GraphNode("Student", 1)
  val studentDecoder: BasicMapDecoder[Student] = BasicMapDecoder[Student]

  val statefulPerson: GraphNode = GraphNode("StatefulPerson", 3, Map("name" -> "some person", "age" -> 45))
  val statefulPersonDecoder: BasicMapDecoder[StatefulPerson] = BasicMapDecoder[StatefulPerson]
}

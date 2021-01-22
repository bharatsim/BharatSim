package com.bharatsim.engine.execution.control

import com.bharatsim.engine.cache.PerTickCache
import com.bharatsim.engine.graph.GraphProvider
import com.bharatsim.engine.testModels.Student
import com.bharatsim.engine.testModels.Student.{studentBehaviour1, studentBehaviour2}
import com.bharatsim.engine.{Context, Dynamics, SimulationConfig}
import org.mockito.MockitoSugar.mock
import org.mockito.{InOrder, Mockito}
import org.scalatest.wordspec.AnyWordSpec

class BehaviourControlTest extends AnyWordSpec {
  "executeFor" should {
    "execute all behaviours of given agent in order" in {
      val context = getContext(2)
      val behaviourControl = new BehaviourControl(context)
      val student = Student()

      behaviourControl.executeFor(student)

      val order: InOrder = Mockito.inOrder(studentBehaviour1, studentBehaviour2)
      order.verify(studentBehaviour1)(context)
      order.verify(studentBehaviour2)(context)
    }
  }
  private def getContext(steps: Int, mockGraphProvider: GraphProvider = mock[GraphProvider], perTickCache: PerTickCache = mock[PerTickCache]) = {
    new Context(mockGraphProvider, new Dynamics, SimulationConfig(steps), perTickCache)
  }
}

package com.bharatsim.engine.intervention

import com.bharatsim.engine.control.{BehaviourControl, StateControl}
import com.bharatsim.engine.graph.GraphProvider
import com.bharatsim.engine.{Context, Dynamics, Simulation, SimulationConfig}
import org.mockito.MockitoSugar.mock
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec

class IntervalBasedInterventionTest extends AnyWordSpec with Matchers {
  "IntervalBasedIntervention" should {
    "create intervention which start at tick x and ends at tick y" in {
      var lastTick = 0
      var startTick = 0
      val perTickFunc = (context: Context) => lastTick = context.getCurrentStep
      val oneTimeFunc = (context: Context) => startTick = context.getCurrentStep
      val intervention =
        IntervalBasedIntervention("dummyIntervention", 2, 5, oneTimeFunc, perTickFunc)
      val context = new Context(mock[GraphProvider], mock[Dynamics], SimulationConfig(10))
      context.interventions.add(intervention)
      val simulation = new Simulation(context, new BehaviourControl(context), new StateControl(context))

      simulation.run()

      startTick shouldBe 2
      lastTick shouldBe 4
    }
  }
}

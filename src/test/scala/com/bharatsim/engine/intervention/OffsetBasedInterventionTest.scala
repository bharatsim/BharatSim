package com.bharatsim.engine.intervention

import com.bharatsim.engine.control.{BehaviourControl, StateControl}
import com.bharatsim.engine.graph.GraphProvider
import com.bharatsim.engine.{Context, Dynamics, Simulation, SimulationConfig}
import org.mockito.MockitoSugar.mock
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec

class OffsetBasedInterventionTest extends AnyWordSpec with Matchers {
  "IntervalBasedIntervention" should {
    "create intervention which ends after n ticks" in {
      val startWhen = (context: Context) => context.getCurrentStep == 2
      var lastTick = 0
      val perTickFunc = (context: Context) => lastTick = context.getCurrentStep
      val intervention =
        OffsetBasedIntervention("dummyIntervention", startWhen, 5, whenActiveActionFunc = perTickFunc)
      val context = new Context(mock[GraphProvider], mock[Dynamics], SimulationConfig(10))
      context.interventions.add(intervention)
      val simulation = new Simulation(context, new BehaviourControl(context), new StateControl(context))

      simulation.run()

      lastTick shouldBe 7
    }
  }
}

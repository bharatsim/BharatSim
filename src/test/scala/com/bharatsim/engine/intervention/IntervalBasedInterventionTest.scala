package com.bharatsim.engine.intervention

import com.bharatsim.engine.Context
import org.mockito.MockitoSugar.{mock, when}
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec

class IntervalBasedInterventionTest extends AnyWordSpec with Matchers {
  "IntervalBasedIntervention" should {
    "create intervention which starts at tick x and ends at tick y" in {
      val perTickFunc = (_: Context) => {}
      val oneTimeFunc = (_: Context) => {}
      val intervention =
        IntervalBasedIntervention("dummyIntervention", 2, 5, oneTimeFunc, perTickFunc)
      val context = mock[Context]
      when(context.getCurrentStep).thenReturn(2, 5)

      intervention.shouldActivate(context) shouldBe true
      intervention.shouldDeactivate(context) shouldBe true
    }
  }
}

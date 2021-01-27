package com.bharatsim.engine.intervention

import com.bharatsim.engine.Context
import org.mockito.MockitoSugar.{mock, spyLambda, verify, when}
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec

class OffsetBasedInterventionTest extends AnyWordSpec with Matchers {
  "OffsetBasedIntervention" should {
    "create intervention which ends after n ticks" in {
      val startWhen = (context: Context) => context.getCurrentStep == 2
      val firstTimeAction = spyLambda((_: Context) => {})
      val intervention =
        OffsetBasedIntervention("dummyIntervention", startWhen, endAfterNTicks = 5, firstTimeAction)

      val context = mock[Context]
      when(context.getCurrentStep).thenReturn(2, 2, 8)
      intervention.shouldActivate(context) shouldBe true
      intervention.firstTimeAction(context)
      intervention.startedAt shouldBe 2
      verify(firstTimeAction).apply(context)
      intervention.shouldDeactivate(context) shouldBe true
    }
  }
}

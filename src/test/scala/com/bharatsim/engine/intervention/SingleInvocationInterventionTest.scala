package com.bharatsim.engine.intervention

import com.bharatsim.engine.Context
import org.mockito.MockitoSugar.{mock, spyLambda, verify}
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec

class SingleInvocationInterventionTest extends AnyWordSpec with Matchers {
  "SingleInvocationIntervention" should {
    "have invokedOnce defaulted to false" in {
      val intervention: SingleInvocationIntervention = createSingleInvocationIntervention()

      intervention.invokedOnce shouldBe false
    }

    "set invokedOnce to true when invoked for first time" in {
      val intervention: SingleInvocationIntervention = createSingleInvocationIntervention()
      val context = mock[Context]
      intervention.firstTimeAction(context)

      intervention.invokedOnce shouldBe true
    }

    "invoke provided firstTimeAction when invoked for first time" in {
      val contextToUnit: Context => Unit = (_: Context) => {}
      val firstTimeAction = spyLambda(contextToUnit)
      val intervention: SingleInvocationIntervention = createSingleInvocationIntervention(firstTimeAction)
      val context = mock[Context]
      intervention.firstTimeAction(context)

      verify(firstTimeAction).apply(context)
    }

    "shouldActivate should return false" when {
      "invokedOnce is true" in {
        val intervention: SingleInvocationIntervention = createSingleInvocationIntervention()
        val context = mock[Context]
        intervention.invokedOnce = true

        intervention.shouldActivate(context) shouldBe false
      }

      "shouldActivate function returns false" in {
        val intervention: SingleInvocationIntervention = createSingleInvocationIntervention()
        val context = mock[Context]

        intervention.shouldActivate(context) shouldBe false
      }
    }
  }

  private def createSingleInvocationIntervention(customFirstTimeAction: Context => Unit = (_: Context) => {}) = {
    val shouldActivate = (_: Context) => false
    val shouldDeactivate = (context: Context) => context.getCurrentStep % 2 == 1
    val firstTimeAction = customFirstTimeAction

    val intervention =
      SingleInvocationIntervention("testIntervention", shouldActivate, shouldDeactivate, firstTimeAction)
    intervention
  }
}

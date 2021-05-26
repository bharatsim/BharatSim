package com.bharatsim.model12Hr.diseaseState

import com.bharatsim.engine.Context
import com.bharatsim.engine.basicConversions.decoders.DefaultDecoders._
import com.bharatsim.engine.basicConversions.encoders.DefaultEncoders._
import com.bharatsim.engine.fsm.State
import com.bharatsim.engine.models.StatefulAgent
import com.bharatsim.engine.utils.Probability.biasedCoinToss
import com.bharatsim.model12Hr.InfectionSeverity.{InfectionSeverity, Mild, Severe}
import com.bharatsim.model12Hr.InfectionStatus.Exposed
import com.bharatsim.model12Hr.{Disease, Person}

case class ExposedState(severeInfectionPercentage: Double, isAsymptomatic: Boolean, exposedDuration: Double)
    extends State {

  private val infectionSeverity = getInfectionSeverity(severeInfectionPercentage)

  private def getInfectionSeverity(severeInfectionPercentage: Double): InfectionSeverity = {
    if (biasedCoinToss(severeInfectionPercentage)) {
      return Severe
    }
    Mild
  }

  override def enterAction(context: Context, agent: StatefulAgent): Unit = {
    agent.updateParam("infectionState", Exposed)
    agent.updateParam("infectedAtTick", context.getCurrentStep)
  }

  private def checkForExposure(context: Context, agent: StatefulAgent): Boolean = {
    context.getCurrentStep >= agent.asInstanceOf[Person].infectedAtTick + exposedDuration * Disease.inverse_dt
  }

  private def checkForPreSymptomatic(context: Context, agent: StatefulAgent): Boolean = {
    if (checkForExposure(context, agent) && !isAsymptomatic) {
      return true
    }
    false
  }

  private def checkForAsymptomatic(context: Context, agent: StatefulAgent): Boolean = {
    if (checkForExposure(context, agent) && isAsymptomatic) {
      return true
    }
    false
  }

  private def getNextCompartmentDuration(context: Context): Double = {
    if (isAsymptomatic) {
      return (Disease.asymptomaticDurationProbabilityDistribution.sample()
        + exposedDuration)
    }
    Disease.presymptomaticDurationProbabilityDistribution.sample() + exposedDuration
  }

  addTransition(
    when = checkForPreSymptomatic,
    to = context => PreSymptomaticState(infectionSeverity, getNextCompartmentDuration(context))
  )

  addTransition(
    when = checkForAsymptomatic,
    to = context => AsymptomaticState(getNextCompartmentDuration(context))
  )
}

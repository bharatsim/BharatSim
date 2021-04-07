package com.bharatsim.model.diseaseState

import com.bharatsim.engine.Context
import com.bharatsim.engine.basicConversions.decoders.DefaultDecoders._
import com.bharatsim.engine.basicConversions.encoders.DefaultEncoders._
import com.bharatsim.engine.fsm.State
import com.bharatsim.engine.models.StatefulAgent
import com.bharatsim.engine.utils.Probability.biasedCoinToss
import com.bharatsim.model.InfectionSeverity.{InfectionSeverity, Mild, Severe}
import com.bharatsim.model.InfectionStatus.Exposed
import com.bharatsim.model.{Disease, Person}

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
  }

  private def checkForExposure(agent: StatefulAgent) = {
    agent.asInstanceOf[Person].infectionTicks >= exposedDuration * Disease.inverse_dt
  }

  private def checkForPreSymptomatic(context: Context, agent: StatefulAgent): Boolean = {
    if (checkForExposure(agent) && !isAsymptomatic) {
      return true
    }
    false
  }

  private def checkForAsymptomatic(context: Context, agent: StatefulAgent): Boolean = {
    if (checkForExposure(agent) && isAsymptomatic) {
      return true
    }
    false
  }

  private def getNextCompartmentDuration(context: Context): Double = {
    if (isAsymptomatic) {
      return (context.dynamics.asInstanceOf[Disease.type].asymptomaticDurationProbabilityDistribution.sample()
        + exposedDuration)
    }
    context.dynamics.asInstanceOf[Disease.type].presymptomaticDurationProbabilityDistribution.sample() + exposedDuration
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

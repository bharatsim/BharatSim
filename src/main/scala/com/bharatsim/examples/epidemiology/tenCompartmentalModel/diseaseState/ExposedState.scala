package com.bharatsim.examples.epidemiology.tenCompartmentalModel.diseaseState

import com.bharatsim.engine.Context
import com.bharatsim.engine.basicConversions.decoders.DefaultDecoders._
import com.bharatsim.engine.basicConversions.encoders.DefaultEncoders._
import com.bharatsim.engine.fsm.State
import com.bharatsim.engine.models.StatefulAgent
import com.bharatsim.engine.utils.Probability.biasedCoinToss
import com.bharatsim.examples.epidemiology.tenCompartmentalModel.InfectionSeverity.{InfectionSeverity, Mild, Severe}
import com.bharatsim.examples.epidemiology.tenCompartmentalModel.InfectionStatus.Exposed
import com.bharatsim.examples.epidemiology.tenCompartmentalModel.{Disease, Person}

case class ExposedState(
    exposedDuration: Double,
    var isAsymptomatic: Boolean = true,
    var infectionSeverity: InfectionSeverity = Mild
) extends State {

  private def getInfectionSeverity(severeInfectionPercentage: Double): InfectionSeverity = {
    if (biasedCoinToss(severeInfectionPercentage)) {
      return Severe
    }
    Mild
  }

  override def perTickAction(context: Context, agent: StatefulAgent): Unit = {
    isAsymptomatic = biasedCoinToss(agent.asInstanceOf[Person].gamma)
    infectionSeverity = getInfectionSeverity(1 - agent.asInstanceOf[Person].delta)
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

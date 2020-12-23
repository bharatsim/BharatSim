package com.bharatsim.model.diseaseState

import com.bharatsim.engine.Context
import com.bharatsim.engine.basicConversions.decoders.DefaultDecoders._
import com.bharatsim.engine.basicConversions.encoders.DefaultEncoders._
import com.bharatsim.engine.fsm.State
import com.bharatsim.engine.models.StatefulAgent
import com.bharatsim.engine.utils.Probability.biasedCoinToss
import com.bharatsim.model.InfectionStatus.Exposed
import com.bharatsim.model.{Disease, Person}

case class ExposedState() extends State{

  private var isAsymptomatic: Boolean = false

  override def enterAction(context: Context, agent: StatefulAgent): Unit = {
    if(agent.activeState == SusceptibleState()) {
      isAsymptomatic = biasedCoinToss(context.dynamics.asInstanceOf[Disease.type].asymptomaticPopulationPercentage)
      agent.updateParam("infectionState", Exposed)
    }
  }

  private def checkForExposure(context: Context, agent: StatefulAgent) = {
    agent.activeState == ExposedState() &&
      agent.asInstanceOf[Person].infectionDay == context.dynamics.asInstanceOf[Disease.type].exposedDuration
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

  addTransition(
    when = checkForAsymptomatic,
    to = context => AsymptomaticState()
  )

  addTransition(
    when = checkForPreSymptomatic,
    to = context => PreSymptomaticState()
  )
}

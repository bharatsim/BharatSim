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

case class ExposedState(severeInfectionPercentage: Double, isAsymptomatic: Boolean) extends State{

  private val infectionSeverity = getInfectionSeverity(severeInfectionPercentage)

  private def getInfectionSeverity(severeInfectionPercentage: Double): InfectionSeverity = {
    if (biasedCoinToss(severeInfectionPercentage)){
      return Severe
    }
    Mild
  }

  override def enterAction(context: Context, agent: StatefulAgent): Unit = {
      agent.updateParam("infectionState", Exposed)
  }

  private def checkForExposure(context: Context, agent: StatefulAgent) = {
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
    when = checkForPreSymptomatic,
    to = context => PreSymptomaticState(infectionSeverity)
  )

  addTransition(
    when = checkForAsymptomatic,
    to = context => AsymptomaticState()
  )
}

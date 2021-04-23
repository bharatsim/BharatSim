package com.bharatsim.model12Hr.diseaseState

import com.bharatsim.engine.Context
import com.bharatsim.engine.basicConversions.decoders.DefaultDecoders._
import com.bharatsim.engine.basicConversions.encoders.DefaultEncoders._
import com.bharatsim.engine.fsm.State
import com.bharatsim.engine.models.StatefulAgent
import com.bharatsim.engine.utils.Probability.biasedCoinToss
import com.bharatsim.model12Hr.InfectionSeverity.{InfectionSeverity, Mild, Severe}
import com.bharatsim.model12Hr.InfectionStatus.{InfectedMild, InfectedSevere}
import com.bharatsim.model12Hr.{Disease, Person}

case class InfectedState(severity: InfectionSeverity, infectedDuration: Double) extends State {

  override def enterAction(context: Context, agent: StatefulAgent): Unit = {
    if (severity == Mild) {
      agent.updateParam("infectionState", InfectedMild)
    } else if (severity == Severe) {
      agent.updateParam("infectionState", InfectedSevere)
    }
  }

  private def checkInfectionLastDay(context: Context, agent: StatefulAgent): Boolean = {
    context.getCurrentStep >= agent.asInstanceOf[Person].infectedAtTick + infectedDuration * Disease.inverse_dt
  }

  def checkForRecovery(context: Context, agent: StatefulAgent): Boolean = {
    if (checkInfectionLastDay(context, agent)) {
      return true
    }
    false
  }

  def checkForDeceased(context: Context, agent: StatefulAgent): Boolean = {
    if (agent.asInstanceOf[Person].isSevereInfected && checkInfectionLastDay(context, agent)) {
      if (biasedCoinToss(context.dynamics.asInstanceOf[Disease.type].deathRate)) {
        return true
      }
    }
    false
  }

  addTransition(
    when = checkForDeceased,
    to = context => DeceasedState()
  )

  addTransition(
    when = checkForRecovery,
    to = context => RecoveredState()
  )
}

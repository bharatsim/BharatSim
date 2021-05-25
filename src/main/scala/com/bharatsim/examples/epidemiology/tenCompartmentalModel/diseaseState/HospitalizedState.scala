package com.bharatsim.examples.epidemiology.tenCompartmentalModel.diseaseState

import com.bharatsim.engine.Context
import com.bharatsim.engine.basicConversions.decoders.DefaultDecoders._
import com.bharatsim.engine.basicConversions.encoders.DefaultEncoders._
import com.bharatsim.engine.fsm.State
import com.bharatsim.engine.models.StatefulAgent
import com.bharatsim.engine.utils.Probability.biasedCoinToss
import com.bharatsim.examples.epidemiology.tenCompartmentalModel.InfectionStatus.Hospitalized
import com.bharatsim.examples.epidemiology.tenCompartmentalModel.{Disease, Person}

case class HospitalizedState(hospitalizedDuration: Double, var isDead: Boolean = false) extends State {

  override def perTickAction(context: Context, agent: StatefulAgent): Unit = {
    isDead = biasedCoinToss(agent.asInstanceOf[Person].sigma)
  }

  override def enterAction(context: Context, agent: StatefulAgent): Unit = {
    agent.updateParam("infectionState", Hospitalized)
  }

  private def checkInfectionLastDay(context: Context, agent: StatefulAgent): Boolean = {
    context.getCurrentStep >= agent.asInstanceOf[Person].infectedAtTick + hospitalizedDuration * Disease.inverse_dt
  }

  def checkForRecovery(context: Context, agent: StatefulAgent): Boolean = {
    if (checkInfectionLastDay(context, agent) && !isDead) {
      return true
    }
    false
  }

  def checkForDeceased(context: Context, agent: StatefulAgent): Boolean = {
    if (checkInfectionLastDay(context, agent) && isDead) {
      return true
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

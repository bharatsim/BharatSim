package com.bharatsim.examples.epidemiology.tenCompartmentalModel.diseaseState

import com.bharatsim.engine.Context
import com.bharatsim.engine.basicConversions.decoders.DefaultDecoders._
import com.bharatsim.engine.basicConversions.encoders.DefaultEncoders._
import com.bharatsim.engine.fsm.State
import com.bharatsim.engine.models.StatefulAgent
import com.bharatsim.engine.utils.Probability.biasedCoinToss
import com.bharatsim.examples.epidemiology.tenCompartmentalModel.InfectionSeverity.{InfectionSeverity, Mild, Severe}
import com.bharatsim.examples.epidemiology.tenCompartmentalModel.InfectionStatus.{InfectedMild, InfectedSevere}
import com.bharatsim.examples.epidemiology.tenCompartmentalModel.{Disease, Person}

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

  def checkForHospitalized(context: Context, agent: StatefulAgent): Boolean = {
    if (checkInfectionLastDay(context, agent) && agent.asInstanceOf[Person].isSevereInfected) {
      return true
    }
    false
  }

  def checkForRecovered(context: Context, agent: StatefulAgent): Boolean = {
    if (checkInfectionLastDay(context, agent) && agent.asInstanceOf[Person].isMildInfected) {
      return true
    }
    false
  }

  addTransition(
    when = checkForRecovered,
    to = context => RecoveredState()
  )

  addTransition(
    when = checkForHospitalized,
    to = context =>
      HospitalizedState(infectedDuration + Disease.criticalSymptomaticDurationProbabilityDistribution.sample())
  )
}

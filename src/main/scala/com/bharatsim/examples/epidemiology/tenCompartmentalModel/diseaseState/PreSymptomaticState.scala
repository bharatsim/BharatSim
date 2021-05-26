package com.bharatsim.examples.epidemiology.tenCompartmentalModel.diseaseState

import com.bharatsim.engine.Context
import com.bharatsim.engine.basicConversions.decoders.DefaultDecoders._
import com.bharatsim.engine.basicConversions.encoders.DefaultEncoders._
import com.bharatsim.engine.fsm.State
import com.bharatsim.engine.models.StatefulAgent
import com.bharatsim.examples.epidemiology.tenCompartmentalModel.InfectionSeverity.{InfectionSeverity, Mild}
import com.bharatsim.examples.epidemiology.tenCompartmentalModel.InfectionStatus.PreSymptomatic
import com.bharatsim.examples.epidemiology.tenCompartmentalModel.{Disease, Person}

case class PreSymptomaticState(infectionSeverity: InfectionSeverity, preSymptomaticDuration: Double) extends State {

  override def enterAction(context: Context, agent: StatefulAgent): Unit = {
    agent.updateParam("infectionState", PreSymptomatic)
  }

  def checkForSymptoms(context: Context, agent: StatefulAgent): Boolean = {
    if (
      context.getCurrentStep >= agent.asInstanceOf[Person].infectedAtTick + preSymptomaticDuration * Disease.inverse_dt
    ) {
      return true
    }
    false
  }

  private def getInfectionDuration(context: Context, infectionSeverity: InfectionSeverity): Double = {
    if (infectionSeverity == Mild) {
      return (Disease.mildSymptomaticDurationProbabilityDistribution.sample() + preSymptomaticDuration)
    }
    Disease.severeSymptomaticDurationProbabilityDistribution.sample() + preSymptomaticDuration
  }

  addTransition(
    when = checkForSymptoms,
    to = context => InfectedState(infectionSeverity, getInfectionDuration(context, infectionSeverity))
  )
}

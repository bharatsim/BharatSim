package com.bharatsim.model.diseaseState

import com.bharatsim.engine.Context
import com.bharatsim.engine.basicConversions.decoders.DefaultDecoders._
import com.bharatsim.engine.basicConversions.encoders.DefaultEncoders._
import com.bharatsim.engine.fsm.State
import com.bharatsim.engine.models.StatefulAgent
import com.bharatsim.model.InfectionSeverity.{InfectionSeverity, Mild}
import com.bharatsim.model.InfectionStatus.PreSymptomatic
import com.bharatsim.model.{Disease, Person}

case class PreSymptomaticState(infectionSeverity: InfectionSeverity, preSymptomaticDuration: Double) extends State {

  override def enterAction(context: Context, agent: StatefulAgent): Unit = {
    agent.updateParam("infectionState", PreSymptomatic)
  }

  def checkForInfectionSeverity(context: Context, agent: StatefulAgent): Boolean = {
    if (context.getCurrentStep >= agent.asInstanceOf[Person].infectedAtTick + preSymptomaticDuration * Disease.inverse_dt) {
      return true
    }
    false
  }

  private def getInfectionDuration(context: Context, infectionSeverity: InfectionSeverity): Double = {
    if (infectionSeverity == Mild) {
      return (context.dynamics
        .asInstanceOf[Disease.type]
        .mildSymptomaticDurationProbabilityDistribution
        .sample() + preSymptomaticDuration)
    }
    context.dynamics
      .asInstanceOf[Disease.type]
      .severeSymptomaticDurationProbabilityDistribution
      .sample() + preSymptomaticDuration
  }

  addTransition(
    when = checkForInfectionSeverity,
    to = context => InfectedState(infectionSeverity, getInfectionDuration(context, infectionSeverity))
  )
}

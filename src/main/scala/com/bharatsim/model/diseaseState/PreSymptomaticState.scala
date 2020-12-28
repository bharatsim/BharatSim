package com.bharatsim.model.diseaseState

import com.bharatsim.engine.Context
import com.bharatsim.engine.basicConversions.decoders.DefaultDecoders._
import com.bharatsim.engine.basicConversions.encoders.DefaultEncoders._
import com.bharatsim.engine.fsm.State
import com.bharatsim.engine.models.StatefulAgent
import com.bharatsim.model.InfectionSeverity.InfectionSeverity
import com.bharatsim.model.InfectionStatus.PreSymptomatic
import com.bharatsim.model.{Disease, Person}

case class PreSymptomaticState(infectionSeverity: InfectionSeverity) extends State {

  override def enterAction(context: Context, agent: StatefulAgent): Unit = {
      agent.updateParam("infectionState", PreSymptomatic)
  }

  def checkForInfectionSeverity(context: Context, agent: StatefulAgent): Boolean = {
    if (
      agent.asInstanceOf[Person].infectionDay == context.dynamics.asInstanceOf[Disease.type].preSymptomaticDuration) {
      return true
    }
    false
  }

  addTransition(
    when = checkForInfectionSeverity,
    to = context => InfectedState(infectionSeverity)
  )
}

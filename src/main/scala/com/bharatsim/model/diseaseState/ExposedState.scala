package com.bharatsim.model.diseaseState

import com.bharatsim.engine.Context
import com.bharatsim.engine.fsm.State
import com.bharatsim.engine.models.StatefulAgent
import com.bharatsim.engine.basicConversions.decoders.DefaultDecoders._
import com.bharatsim.engine.basicConversions.encoders.DefaultEncoders._
import com.bharatsim.model.{Disease, Person}
import com.bharatsim.model.InfectionStatus.Infected

case class ExposedState() extends State{

  private def checkForInfection(context: Context, agent: StatefulAgent): Boolean = {
    if (agent.asInstanceOf[Person].isExposed &&
      agent.asInstanceOf[Person].infectionDay == context.dynamics.asInstanceOf[Disease.type].exposedDuration) {
      agent.updateParam("infectionState", Infected)
      return true
    }
    return false
  }

  addTransition(
    when = checkForInfection,
    to = context => InfectedState()
  )
}

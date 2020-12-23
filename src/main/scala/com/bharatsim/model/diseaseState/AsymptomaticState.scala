package com.bharatsim.model.diseaseState

import com.bharatsim.engine.Context
import com.bharatsim.engine.basicConversions.decoders.DefaultDecoders._
import com.bharatsim.engine.basicConversions.encoders.DefaultEncoders._
import com.bharatsim.engine.fsm.State
import com.bharatsim.engine.models.StatefulAgent
import com.bharatsim.model.InfectionStatus.Asymptomatic
import com.bharatsim.model.{Disease, Person}

case class AsymptomaticState() extends State {

  override def enterAction(context: Context, agent: StatefulAgent): Unit = {

    if(agent.activeState == ExposedState()) {
      agent.updateParam("infectionState", Asymptomatic)
    }
  }

  def checkForRecovery(context: Context, agent: StatefulAgent): Boolean = {
    if (agent.activeState == AsymptomaticState() &&
      agent.asInstanceOf[Person].infectionDay > context.dynamics.asInstanceOf[Disease.type]
        .lastDay) {
      return true
    }
    false
  }

  addTransition(
    when = checkForRecovery,
    to = context => RecoveredState()
  )
}

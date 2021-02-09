package com.bharatsim.model.diseaseState

import com.bharatsim.engine.Context
import com.bharatsim.engine.basicConversions.decoders.DefaultDecoders._
import com.bharatsim.engine.basicConversions.encoders.DefaultEncoders._
import com.bharatsim.engine.fsm.State
import com.bharatsim.engine.models.StatefulAgent
import com.bharatsim.model.InfectionStatus.Asymptomatic
import com.bharatsim.model.Person

case class AsymptomaticState(asymptomaticDuration: Double) extends State {

  override def enterAction(context: Context, agent: StatefulAgent): Unit =
    agent.updateParam("infectionState", Asymptomatic)

  def checkForRecovery(context: Context, agent: StatefulAgent): Boolean = {
    if (
      agent.asInstanceOf[Person].isAsymptomatic &&
      agent.asInstanceOf[Person].infectionDay >= asymptomaticDuration
    ) {
      return true
    }
    false
  }

  addTransition(
    when = checkForRecovery,
    to = context => RecoveredState()
  )
}

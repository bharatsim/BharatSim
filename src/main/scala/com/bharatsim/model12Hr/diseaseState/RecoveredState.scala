package com.bharatsim.model12Hr.diseaseState

import com.bharatsim.engine.Context
import com.bharatsim.engine.fsm.State
import com.bharatsim.engine.models.StatefulAgent
import com.bharatsim.model12Hr.InfectionStatus.Recovered

case class RecoveredState() extends State {

  override def enterAction(context: Context, agent: StatefulAgent): Unit = {
    agent.updateParam("infectionState", Recovered)
  }

}

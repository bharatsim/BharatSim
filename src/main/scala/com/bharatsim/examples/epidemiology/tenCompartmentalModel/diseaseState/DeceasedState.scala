package com.bharatsim.examples.epidemiology.tenCompartmentalModel.diseaseState

import com.bharatsim.engine.Context
import com.bharatsim.engine.fsm.State
import com.bharatsim.engine.models.StatefulAgent
import com.bharatsim.examples.epidemiology.tenCompartmentalModel.InfectionStatus.Deceased

case class DeceasedState() extends State {

  override def enterAction(context: Context, agent: StatefulAgent): Unit = {
    agent.updateParam("infectionState", Deceased)
  }

}

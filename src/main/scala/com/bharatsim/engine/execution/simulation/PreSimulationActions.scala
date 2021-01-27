package com.bharatsim.engine.execution.simulation

import com.bharatsim.engine.Context
import com.bharatsim.engine.listeners.SimulationListenerRegistry
import com.bharatsim.engine.models.{Agent, StatefulAgent}

class PreSimulationActions(context: Context) {
  def execute(): Unit = {
    SimulationListenerRegistry.notifySimulationStart(context)
    executeStateEnterActions()
  }

  private def executeStateEnterActions(): Unit = {
    context.registeredNodesWithDecoder
      .map(_.toAgent)
      .foreach((agent: Agent) => {
        agent match {
          case statefulAgent: StatefulAgent =>
            statefulAgent.activeState.enterAction(context, statefulAgent)
          case _ =>
        }
      })
  }
}

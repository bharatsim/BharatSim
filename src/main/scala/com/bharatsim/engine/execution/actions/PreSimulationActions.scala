package com.bharatsim.engine.execution.actions

import com.bharatsim.engine.Context
import com.bharatsim.engine.listeners.SimulationListenerRegistry
import com.bharatsim.engine.models.StatefulAgent

class PreSimulationActions(context: Context) {
  def execute(): Unit = {
    SimulationListenerRegistry.notifySimulationStart(context)
    executeStateEnterActions()
  }

  private def executeStateEnterActions(): Unit = {
    context.agentLabels.foreach(label => {
      val nodes = context.graphProvider.fetchNodes(label, Map.empty[String, Any])
      nodes.foreach {
        case statefulAgent: StatefulAgent =>
          statefulAgent.activeState.enterAction(context, statefulAgent)
        case _ =>
      }
    })
  }
}

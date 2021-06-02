package com.bharatsim.engine.execution.actions

import com.bharatsim.engine.Context
import com.bharatsim.engine.listeners.SimulationListenerRegistry
import com.bharatsim.engine.models.StatefulAgent
import com.typesafe.scalalogging.LazyLogging

class PreSimulationActions(context: Context) extends LazyLogging {
  def execute(): Unit = {
    SimulationListenerRegistry.notifySimulationStart(context)
    executeStateEnterActions()
    logger.debug("Done PreSimulationActions")
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

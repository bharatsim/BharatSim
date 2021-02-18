package com.bharatsim.engine.execution.simulation

import com.bharatsim.engine.Context
import com.bharatsim.engine.listeners.SimulationListenerRegistry
import com.bharatsim.engine.models.StatefulAgent

import scala.annotation.tailrec

class PreSimulationActions(context: Context) {
  def execute(): Unit = {
    SimulationListenerRegistry.notifySimulationStart(context)
    executeStateEnterActions()
  }

  def processByParts(): Unit = {
    @tailrec
    def fetchAllNodes(label: String, skip: Int, limit: Int): Unit = {
      val nodes = context.graphProvider.fetchNodesWithSkipAndLimit(label, Map.empty, skip, limit)

      nodes.foreach {
        case statefulAgent: StatefulAgent =>
          statefulAgent.activeState.enterAction(context, statefulAgent)
        case _ =>
      }

      if (nodes.nonEmpty) {
        fetchAllNodes(label, skip + nodes.size, limit)
      }
    }

    context.agentLabels.foreach(label => fetchAllNodes(label, 0, 1000))
  }

  private def executeStateEnterActions(): Unit = {
    processByParts()
  }
}

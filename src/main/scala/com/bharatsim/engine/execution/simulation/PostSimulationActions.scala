package com.bharatsim.engine.execution.simulation

import com.bharatsim.engine.Context
import com.bharatsim.engine.listeners.SimulationListenerRegistry

class PostSimulationActions(context: Context) {
  def execute(): Unit = {
    SimulationListenerRegistry.notifySimulationEnd(context)
  }
}

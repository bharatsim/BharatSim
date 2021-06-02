package com.bharatsim.engine.execution.actions

import com.bharatsim.engine.Context
import com.bharatsim.engine.listeners.SimulationListenerRegistry
import com.typesafe.scalalogging.LazyLogging

class PostSimulationActions(context: Context) extends LazyLogging {
  def execute(): Unit = {
    SimulationListenerRegistry.notifySimulationEnd(context)
    logger.debug("Done PostSimulationActions")
  }
}

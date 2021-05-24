package com.bharatsim.engine.execution.executors

import com.bharatsim.engine.execution.SimulationDefinition

trait SimulationExecutor {
  def execute(simulationDefinition: SimulationDefinition): Unit
}

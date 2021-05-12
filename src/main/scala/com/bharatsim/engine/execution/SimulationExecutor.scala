package com.bharatsim.engine.execution

trait SimulationExecutor {
  def execute(simulationDefinition: SimulationDefinition): Unit
}

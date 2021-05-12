package com.bharatsim.engine

import com.bharatsim.engine.execution.executors.{ActorBasedExecutor, DefaultExecutor, DistributedExecutor}
import com.bharatsim.engine.execution.{SimulationDefinition, SimulationExecutor}
import com.typesafe.scalalogging.LazyLogging

class DistributedSimulation extends LazyLogging {
  private var ingestionStep: Context => Unit = _ => {}
  private var simulationBody: Context => Unit = _ => {}
  private var onComplete: Context => Unit = _ => {}
  private val executionMode = ApplicationConfigFactory.config.executionMode
  def ingestData(f: (Context) => Unit): Unit = {
    ingestionStep = f
  }

  def defineSimulation(f: Context => Unit): Unit = {
    simulationBody = f
  }

  def onCompleteSimulation(f: Context => Unit): Unit = {
    onComplete = f
  }

  private def getExecutor: SimulationExecutor = {
    logger.info("Execution mode {}", executionMode)
    executionMode match {
      case Distributed => new DistributedExecutor
      case ActorBased  => new ActorBasedExecutor
      case _           => new DefaultExecutor
    }
  }

  def run(): Unit = {
    val simulationDef = SimulationDefinition(ingestionStep, simulationBody, onComplete)
    getExecutor.execute(simulationDef)
  }

}

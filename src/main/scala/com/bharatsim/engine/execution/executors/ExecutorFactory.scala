package com.bharatsim.engine.execution.executors

import com.bharatsim.engine.{ActorBased, ApplicationConfig, ApplicationConfigFactory, Distributed}
import com.typesafe.scalalogging.LazyLogging

class ExecutorFactory(config: ApplicationConfig = ApplicationConfigFactory.config) extends LazyLogging {

  def getExecutor(): SimulationExecutor = {
    logger.info("Execution mode {}", config.executionMode)
    config.executionMode match {
      case Distributed => new DistributedExecutor
      case ActorBased  => new ActorBasedExecutor
      case _           => new DefaultExecutor
    }
  }

}

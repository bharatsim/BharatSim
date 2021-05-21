package com.bharatsim.engine.execution.executors

import akka.actor.typed.ActorSystem
import com.bharatsim.engine.distributed.Guardian
import com.bharatsim.engine.execution.{SimulationDefinition, SimulationExecutor}
import com.typesafe.config.ConfigFactory

class DistributedExecutor(guardian: Guardian = new Guardian()) extends SimulationExecutor {
  override def execute(simulationDefinition: SimulationDefinition): Unit = {
    val config = ConfigFactory.load("cluster").withFallback(ConfigFactory.load())
    ActorSystem[Nothing](guardian.start(simulationDefinition), "Cluster", config)
  }
}

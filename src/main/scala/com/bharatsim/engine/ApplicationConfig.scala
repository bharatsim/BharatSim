package com.bharatsim.engine

import com.typesafe.config.ConfigFactory

class ApplicationConfig {
  private val config = ConfigFactory.load()

  val executionMode: ExecutionMode = {
    val mode = config.getString("bharatsim.engine.execution.mode")
    mode match {
      case "collection-based" => CollectionBased
      case "actor-based" => ActorBased
      case "distributed" => Distributed
      case _ => NoParallelism
    }
  }

  val numProcessingActors: Int = config.getInt("bharatsim.engine.execution.actor-based.num-processing-actors")
}

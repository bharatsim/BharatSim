package com.bharatsim.engine

import com.typesafe.config.ConfigFactory

sealed trait ParallelismMode

case object NoParallelism extends ParallelismMode

case object CollectionBased extends ParallelismMode

case object ActorBased extends ParallelismMode

class ApplicationConfig {
  private val config = ConfigFactory.load()

  val parallelism: ParallelismMode = {
    val mode = config.getString("bharatsim.engine.execution.parallelism")
    mode match {
      case "collection-based" => CollectionBased
      case "actor-based" => ActorBased
      case _ => NoParallelism
    }
  }

  val numProcessingActors: Int = config.getInt("bharatsim.engine.execution.actor-based.num-processing-actors")
}

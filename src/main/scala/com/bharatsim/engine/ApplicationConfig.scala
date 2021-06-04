package com.bharatsim.engine

import com.bharatsim.engine.distributed.Role
import com.typesafe.config.ConfigFactory

class ApplicationConfig {
  private lazy val config = ConfigFactory.load()
  private lazy val clusterConfig = ConfigFactory.load("cluster.conf")

  lazy val workBatchSize: Int = config.getInt("bharatsim.engine.distributed.work-batch-size")

  lazy val agentProcessParallelism: Int = config.getInt("bharatsim.engine.distributed.agent-process-parallelism")
  lazy val askTimeout: Int = config.getInt("bharatsim.engine.distributed.ask-timeout")
  lazy val ingestionBatchSize: Int = config.getInt("bharatsim.engine.ingestion.batch-size")
  lazy val ingestionMapParallelism: Int = config.getInt("bharatsim.engine.ingestion.map-parallelism")

  lazy val disableIngestion: Boolean = config.getBoolean("bharatsim.engine.distributed.debug.disable-ingestion")
  lazy val ingestionOnly: Boolean = config.getBoolean("bharatsim.engine.distributed.debug.ingestion-only")

  lazy val role: Role.Value = Role.withName(clusterConfig.getStringList("akka.cluster.roles").get(0))

  lazy val executionMode: ExecutionMode = {
    val mode = config.getString("bharatsim.engine.execution.mode")
    mode match {
      case "collection-based" => CollectionBased
      case "actor-based"      => ActorBased
      case "distributed"      => Distributed
      case _                  => NoParallelism
    }
  }

  val simulationSteps: Int = config.getInt("bharatsim.engine.execution.simulation-steps")

  def hasEngineMainRole(): Boolean = {
    executionMode == Distributed && role == Role.EngineMain
  }
  def hasWorkerRole(): Boolean = {
    executionMode == Distributed && role == Role.Worker
  }

  lazy val numProcessingActors: Int = config.getInt("bharatsim.engine.execution.actor-based.num-processing-actors")

  private lazy val neo4jConfig = config.getConfig("bharatsim.engine.db.neo4j")
  lazy val neo4jURI: String = neo4jConfig.getString("uri")
  lazy val neo4jUsername: String = neo4jConfig.getString("username")
  lazy val neo4jPass: String = neo4jConfig.getString("password")
  lazy val neo4jConnectionPoolSize: Int = neo4jConfig.getInt("connection-pool-size")
  lazy val writeParallelism: Int = neo4jConfig.getInt("write-parallelism")
  lazy val readParallelism: Int = neo4jConfig.getInt("read-parallelism")
  lazy val readBatchSize: Int = neo4jConfig.getInt("read-batch-size")
  lazy val readWaitTime: Int = neo4jConfig.getInt("read-wait-time")
  lazy val writeBatchSize: Int = neo4jConfig.getInt("write-batch-size")
  lazy val maxQueryRetry: Int = neo4jConfig.getInt("max_query_retry")
}

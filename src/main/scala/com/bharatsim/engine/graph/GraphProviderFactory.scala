package com.bharatsim.engine.graph

import java.net.URI

import akka.actor.typed.ActorSystem
import com.bharatsim.engine.ApplicationConfigFactory
import com.bharatsim.engine.graph.neo4j.{BatchWriteNeo4jProvider, Neo4jConfig, Neo4jProvider}

private[engine] object GraphProviderFactory {
  private var graphProvider: GraphProvider = null
  private lazy val config = ApplicationConfigFactory.config

  def init(): Unit = {
    graphProvider = new Neo4jProvider(makeNeo4jConfig())
  }

  def initLazyNeo4j(system: ActorSystem[Nothing]): Unit = {
    graphProvider = new BatchWriteNeo4jProvider(makeNeo4jConfig(), config.writeParallelism, system)
  }

  private def makeNeo4jConfig() = {
    new Neo4jConfig(URI.create(config.neo4jURI), Some(config.neo4jUsername), Some(config.neo4jPass))
  }

  def get: GraphProvider = {
    graphProvider
  }

  def testOverride(gp: GraphProvider): Unit = {
    graphProvider = gp
  }
}

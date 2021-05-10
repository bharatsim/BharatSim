package com.bharatsim.engine.graph

import java.net.URI

import com.bharatsim.engine.ApplicationConfigFactory
import com.bharatsim.engine.graph.custom.BufferedGraphWithAutoSync
import com.bharatsim.engine.graph.neo4j.{BatchNeo4jProvider, Neo4jConfig}

private[engine] object GraphProviderFactory {
  private var graphProvider: GraphProvider = null
  private lazy val config = ApplicationConfigFactory.config

  def init(): Unit = {
    graphProvider = BufferedGraphWithAutoSync()
  }

  def initLazyNeo4j(): Unit = {
    graphProvider = new BatchNeo4jProvider(makeNeo4jConfig())
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

package com.bharatsim.engine.graph

import java.net.URI

import com.bharatsim.engine.{ApplicationConfigFactory, Distributed}
import com.bharatsim.engine.graph.custom.BufferedGraphWithAutoSync
import com.bharatsim.engine.graph.neo4j.{BatchNeo4jProvider, Neo4jConfig}

object GraphProviderFactory {
  private var graphProvider: Option[GraphProvider] = None
  private lazy val config = ApplicationConfigFactory.config

  private def init(): Unit = {
    if (graphProvider.isEmpty) {
      config.executionMode match {
        case Distributed => graphProvider = Some(new BatchNeo4jProvider(makeNeo4jConfig()))
        case _           => graphProvider = Some(BufferedGraphWithAutoSync())
      }
    }
  }

  private def makeNeo4jConfig() = {
    new Neo4jConfig(URI.create(config.neo4jURI), Some(config.neo4jUsername), Some(config.neo4jPass))
  }

  def get: GraphProvider = {
    init()
    graphProvider.get
  }

  def testOverride(gp: GraphProvider): Unit = {
    graphProvider = Some(gp)
  }
}

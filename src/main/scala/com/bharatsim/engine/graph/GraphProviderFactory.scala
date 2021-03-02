package com.bharatsim.engine.graph

import java.net.URI

import akka.actor.typed.{ActorRef, ActorSystem}
import com.bharatsim.engine.ApplicationConfigFactory
import com.bharatsim.engine.distributed.store.ActorBasedStore.DBQuery
import com.bharatsim.engine.distributed.store.{ActorBasedGraphProvider, ActorBasedStore}
import com.bharatsim.engine.graph.neo4j.{Neo4jConfig, Neo4jProvider}

private[engine] object GraphProviderFactory {
  private var graphProvider: GraphProvider = null

  def init(): Unit = {
    val config = ApplicationConfigFactory.config
    val neo4jConfig = new Neo4jConfig(URI.create(config.neo4jURI), Some(config.neo4jUsername), Some(config.neo4jPass))
    graphProvider = new Neo4jProvider(neo4jConfig)
  }

  def init(dataStoreRef: ActorRef[DBQuery], system: ActorSystem[Nothing]): Unit = {
    graphProvider = new ActorBasedGraphProvider(dataStoreRef)(system)
  }

  def initDataStore(): Unit = {
    graphProvider = ActorBasedStore.graphProvider
  }

  def get: GraphProvider = {
    graphProvider
  }

  def testOverride(gp: GraphProvider): Unit = {
    graphProvider = gp
  }
}

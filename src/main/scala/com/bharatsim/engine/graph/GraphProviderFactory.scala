package com.bharatsim.engine.graph

import java.net.URI

import akka.actor.typed.{ActorRef, ActorSystem}
import com.bharatsim.engine.{ApplicationConfig, ApplicationConfigFactory}
import com.bharatsim.engine.distributed.store.ActorBasedStore.DBQuery
import com.bharatsim.engine.distributed.store.{ActorBasedGraphProvider, ActorBasedStore}
import com.bharatsim.engine.graph.neo4j.{LazyWriteNeo4jProvider, Neo4jConfig, Neo4jProvider}

private[engine] object GraphProviderFactory {
  private var graphProvider: GraphProvider = null
  private lazy val config = ApplicationConfigFactory.config

  def init(): Unit = {
    graphProvider = new Neo4jProvider(makeNeo4jConfig())
  }

  def init(dataStoreRef: ActorRef[DBQuery], system: ActorSystem[Nothing]): Unit = {
    graphProvider = new ActorBasedGraphProvider(dataStoreRef)(system)
  }

  def initDataStore(): Unit = {
    graphProvider = ActorBasedStore.graphProvider
  }

  def initLazyNeo4j(): Unit = {
    graphProvider = new LazyWriteNeo4jProvider(makeNeo4jConfig(), config.writeParallelism)
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

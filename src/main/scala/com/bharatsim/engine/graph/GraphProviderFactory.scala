package com.bharatsim.engine.graph

import akka.actor.typed.{ActorRef, ActorSystem}
import com.bharatsim.engine.distributed.store.ActorBasedStore.DBQuery
import com.bharatsim.engine.distributed.store.{ActorBasedGraphProvider, ActorBasedStore}
import com.bharatsim.engine.graph.custom.BufferedGraphWithAutoSync

private[engine] object GraphProviderFactory {
  private var graphProvider: GraphProvider = null;

  def init(): Unit = {
    graphProvider = BufferedGraphWithAutoSync()
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

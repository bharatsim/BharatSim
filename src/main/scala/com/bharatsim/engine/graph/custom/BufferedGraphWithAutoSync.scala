package com.bharatsim.engine.graph.custom

import com.bharatsim.engine.Context
import com.bharatsim.engine.listeners.{SimulationListener, SimulationListenerRegistry}

import scala.collection.concurrent.TrieMap

class BufferedGraphWithAutoSync(graphOperations: GraphOperations) extends GraphProviderWithBufferImpl(graphOperations) with SimulationListener {
  SimulationListenerRegistry.register(this)

  override def onSimulationStart(context: Context): Unit = {}

  override def onStepStart(context: Context): Unit = syncBuffers()

  override def onStepEnd(context: Context): Unit = {}

  override def onSimulationEnd(context: Context): Unit = {}
}

object BufferedGraphWithAutoSync {
  def apply(): BufferedGraphWithAutoSync = {
    val idGenerator = new IdGenerator
    val graphOperations =
      new BufferedGraph(Buffer(TrieMap.empty, TrieMap.empty), Buffer(TrieMap.empty, TrieMap.empty), idGenerator)
    new BufferedGraphWithAutoSync(graphOperations)
  }
}
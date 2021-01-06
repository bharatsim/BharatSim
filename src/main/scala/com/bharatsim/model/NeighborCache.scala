package com.bharatsim.model

import com.bharatsim.engine.graph.GraphProvider.NodeId

import scala.collection.mutable

object NeighborCache {
  private val store = mutable.HashMap.empty[String, mutable.HashMap[NodeId, Int]]
  private var currentStep = -1

  def put(place: String, internalId: NodeId, count: NodeId, step: Int): Unit = {
    currentStep = step

    if (store.contains(place)) {
      store(place).put(internalId, count)
    } else {
      store.put(place, mutable.HashMap(internalId -> count))
    }
  }

  def countFor(label: String, nodeId: NodeId, step: Int): Option[Int] = {
    if (step > currentStep) {
      store.clear()
      None
    }

    store.get(label) match {
      case Some(label) => label.get(nodeId)
      case _ => None
    }
  }
}

package com.bharatsim.engine
import com.bharatsim.engine.basicConversions.encoders.BasicEncoder
import com.bharatsim.engine.graph.{GraphNode, GraphProvider, GraphProviderFactory}

class Node()(implicit graphProvider: GraphProvider =  GraphProviderFactory.get) extends Identity {
  override var id: Int = 0

  private[engine] def setId(newId: Int): Unit = {
    id = newId
  }

  def unidirectionalConnect(relation: String, to: Node): Unit = {
    graphProvider.createRelationship(id, relation, to.id)
  }

  def bidirectionalConnect(relation: String, to: Node): Unit = {
    unidirectionalConnect(relation, to)
    to.unidirectionalConnect(relation, this)
  }

  def disconnect(relation: String, to: Node): Unit = {
    graphProvider.deleteRelationship(id, relation, to.id)
  }

  def getConnections(relation: String): Iterator[GraphNode] = {
    graphProvider.fetchNeighborsOf(id, relation).iterator
  }

  def updateParam[T](key: String, value: T)(implicit encoder: BasicEncoder[T]): Unit = {
    graphProvider.updateNode(id, (key, encoder.encode(value).get))
  }
}


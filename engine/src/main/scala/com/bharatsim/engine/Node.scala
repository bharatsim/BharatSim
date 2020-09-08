package com.bharatsim.engine
import com.bharatsim.engine.Node.fromGraphNode
import com.bharatsim.engine.graph.GraphNode

import scala.collection.mutable

class Node()(implicit context: Context) extends Identity {
  override var id: Int = 0
  val params = new mutable.HashMap[String, Any]()

  private[engine] def setId(newId: Int): Unit = {
    id = newId
  }

  private[engine] def setParams(nodeParams: Map[String, Any]): Unit = params.addAll(nodeParams)

  def fetchParam(key: String): Option[Any] = {
    if(params.contains(key)) Some(params(key))
    else None
  }

  def unidirectionalConnect(relation: String, to: Node): Unit = {
    context.graphProvider.createRelationship(relation, id, to.id)
  }

  def bidirectionalConnect(relation: String, to: Node): Unit = {
    unidirectionalConnect(relation, to)
    to.unidirectionalConnect(relation, this)
  }

  def disconnect(relation: String, to: Node): Unit = {
    context.graphProvider.deleteRelationship(relation, id, to.id)
  }

  def getConnections(relation: String): Iterator[Node] = {
    context.graphProvider.fetchNeighborsOf(id, relation).map(fromGraphNode(_)).iterator
  }

  def updateParam(key: String, value: Any): Unit = {
    context.graphProvider.updateNode(id, (key, value))
  }
}

object Node {
  def fromGraphNode(graphNode: GraphNode)(implicit context: Context): Node = {
    val node = new Node()
    node.setId(graphNode.Id)
    node.setParams(graphNode.getParams)
    node
  }
}

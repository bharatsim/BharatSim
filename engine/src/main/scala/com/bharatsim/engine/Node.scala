package com.bharatsim.engine
import com.bharatsim.engine.Node.fromGraphNode
import com.bharatsim.engine.graph.{GraphNode, GraphProvider, GraphProviderFactory}

import scala.collection.mutable
import scala.reflect.{ClassTag, classTag}

class Node()(implicit graphProvider: GraphProvider =  GraphProviderFactory.get) extends Identity {
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
    graphProvider.createRelationship(id, relation, to.id)
  }

  def bidirectionalConnect(relation: String, to: Node): Unit = {
    unidirectionalConnect(relation, to)
    to.unidirectionalConnect(relation, this)
  }

  def disconnect(relation: String, to: Node): Unit = {
    graphProvider.deleteRelationship(id, relation, to.id)
  }

  def getConnections[T: ClassTag](relation: String): Iterator[T] = {
    graphProvider.fetchNeighborsOf(id, relation).map(fromGraphNode[T](_)).iterator
  }

  def updateParam(key: String, value: Any): Unit = {
    graphProvider.updateNode(id, (key, value))
  }
}

object Node {
  def fromGraphNode[T: ClassTag](graphNode: GraphNode): T = {
    val className = classTag[T].runtimeClass.getName
    val value = Class.forName(className)
    val modelInstance = value.getDeclaredConstructor().newInstance().asInstanceOf[Node]
    modelInstance.setId(graphNode.Id)
    modelInstance.setParams(graphNode.getParams)
    modelInstance.asInstanceOf[T]
  }
}

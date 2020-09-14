package com.bharatsim.engine.graph

import com.bharatsim.engine.graph.GraphProvider.NodeId

import scala.collection.mutable

class GraphNodeImpl(override val label: String, id: NodeId, params: Map[String, Any] = Map.empty) extends GraphNode {
  override def Id: NodeId = id

  override def apply(key: String): Option[Any] = params.get(key)

  override def getParams: Map[String, Any] = params
}


case class InternalNode(label: String, id: NodeId, params: Map[String, Any]) {
  private val relationships: mutable.HashMap[String, mutable.HashSet[NodeId]] = mutable.HashMap.empty

  def toGraphNode: GraphNode = new GraphNodeImpl(label, id, params)

  def fetchParam(key: String): Option[Any] = params.get(key)

  def addRelation(label: String, to: NodeId): Unit = {
    if (relationships.contains(label)) {
      relationships(label).add(to)
    } else {
      val hs = new mutable.HashSet[NodeId]()
      hs.add(to)
      relationships.put(label, hs)
    }
  }

  def fetchNeighborsWithLabel(label: String): mutable.HashSet[NodeId] = {
    if(relationships.contains(label)) {
      relationships(label)
    } else mutable.HashSet.empty
  }
}
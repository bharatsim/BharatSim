package com.bharatsim.engine.graph.custom

import com.bharatsim.engine.graph.GraphProvider.NodeId
import com.bharatsim.engine.graph.{GraphNode, GraphNodeImpl}

import scala.collection.mutable

private[engine] case class InternalNode(label: String, id: NodeId, params: mutable.HashMap[String, Any]) {
  private val relationships: mutable.HashMap[String, mutable.HashSet[NodeId]] = mutable.HashMap.empty

  def toGraphNode: GraphNode = new GraphNodeImpl(label, id, params.toMap)

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
    if (relationships.contains(label)) {
      relationships(label)
    } else mutable.HashSet.empty
  }

  def updateProps(props: Map[String, Any]): Unit = {
    props.foreach(kv => params.put(kv._1, kv._2))
  }

  def deleteRelationship(label: String, to: NodeId): Unit = {
    if (relationships.contains(label)) {
      relationships(label).remove(to)
    }
  }
}

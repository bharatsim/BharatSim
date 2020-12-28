package com.bharatsim.engine.graph.custom

import com.bharatsim.engine.graph.GraphProvider.NodeId
import com.bharatsim.engine.graph.{GraphNode, GraphNodeImpl}
import com.typesafe.scalalogging.LazyLogging

import scala.collection.mutable

private[engine] case class InternalNode(label: String, id: NodeId, params: mutable.HashMap[String, Any]) extends LazyLogging {
  private val relationships: mutable.HashMap[String, mutable.HashSet[NodeId]] = mutable.HashMap.empty

  private val incoming = mutable.HashMap.empty[String, mutable.HashSet[NodeId]]

  def fetchIncoming: mutable.Map[String, mutable.HashSet[NodeId]] = incoming

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

  def removeRelation(label: String, idToDelete: NodeId): Unit = {
    relationships.get(label) match {
      case Some(value) => value.remove(idToDelete)
      case _ => logger.debug("Node {} does not have any relationship with label {}", id, label)
    }
  }

  def addIncoming(label: String, from: NodeId): Unit = {
    if (incoming.contains(label)) {
      incoming(label).add(from)
    } else {
      val hs = new mutable.HashSet[NodeId]()
      hs.add(from)
      incoming.put(label, hs)
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

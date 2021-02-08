package com.bharatsim.engine.graph.custom

import com.bharatsim.engine.graph.GraphNode
import com.bharatsim.engine.graph.GraphProvider.NodeId
import com.typesafe.scalalogging.LazyLogging

import scala.collection.immutable._

private[engine] case class InternalNode(label: String, id: NodeId, params: HashMap[String, Any]) extends LazyLogging {
  private var relationships: HashMap[String, HashSet[NodeId]] = HashMap.empty

  private var incoming = HashMap.empty[String, HashSet[NodeId]]

  def fetchIncoming: Map[String, HashSet[NodeId]] = incoming

  def toGraphNode: GraphNode = GraphNode(label, id, params)

  def fetchParam(key: String): Option[Any] = params.get(key)

  def addRelation(label: String, to: NodeId): InternalNode = {

    if (relationships.contains(label)) {
      val relation = relationships(label)
      val toNodes = relation.incl(to)

      return createNew(newRelationships = relationships.updated(label, toNodes))
    } else {
      val hs = new HashSet[NodeId]().incl(to)
      return createNew(newRelationships = relationships.updated(label, hs))

    }
  }

  def removeRelation(label: String, idToDelete: NodeId): InternalNode = {
    if (relationships.contains(label)) {
      val value = relationships(label).excl(idToDelete)
      return createNew(newRelationships = relationships.updated(label, value))
    }
    logger.debug("Node {} does not have any relationship with label {}", id, label)
    this
  }

  def addIncoming(label: String, from: NodeId): InternalNode = {
    if (incoming.contains(label)) {
      return createNew(newIncoming = incoming.updated(label, incoming(label).incl(from)));
    } else {
      val hs = new HashSet[NodeId]().incl(from)
      return createNew(newIncoming = incoming.updated(label, hs));

    }
  }

  def fetchNeighborsWithLabelCount(label: String): Int = {
    if (relationships.contains(label)) {
      return relationships(label).size
    }
    0
  }

  def fetchNeighborsWithLabel(label: String): HashSet[NodeId] = {
    if (relationships.contains(label)) {
      relationships(label)
    } else HashSet.empty
  }

  def updateProps(props: Map[String, Any]): InternalNode = {
    return createNew(newParams = params.concat(props))
  }

  def deleteRelationship(label: String, to: NodeId): InternalNode = {
    if (relationships.contains(label)) {
      return createNew(newRelationships = relationships.updated(label, relationships(label).excl(to)));
    }
    this
  }

  private def createNew(
      newParams: HashMap[String, Any] = params,
      newRelationships: HashMap[String, HashSet[NodeId]] = relationships,
      newIncoming: HashMap[String, HashSet[NodeId]] = incoming
  ): InternalNode = {
    val node = new InternalNode(label, id, newParams)
    node.relationships = newRelationships
    node.incoming = newIncoming
    node
  }
}

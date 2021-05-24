package com.bharatsim.engine.graph.custom

import com.bharatsim.engine.graph.GraphProvider.NodeId
import com.bharatsim.engine.graph.custom.GraphOperations.IndexedNodesType
import com.bharatsim.engine.graph.ingestion.{CsvNode, GraphData, RefToIdMapping, Relation}
import com.github.tototoshi.csv.CSVReader
import com.typesafe.scalalogging.LazyLogging

import scala.collection.{immutable, mutable}

class WriteOperations(buffer: Buffer, emptyNode: () => IndexedNodesType, idGenerator: IdGenerator) extends LazyLogging {
  def createRelationship(node1: NodeId, label: String, node2: NodeId): Unit = {
    val nodeFrom = buffer.indexedNodes.get(node1)
    val nodeTo = buffer.indexedNodes.get(node2)

    (nodeFrom, nodeTo) match {
      case (Some(from), Some(to)) =>
        val updatedFromNode = from.addRelation(label, to.id)
        val updatedToNode = to.addIncoming(label, from.id)
        replaceNode(node1, updatedFromNode)
        replaceNode(node2, updatedToNode)
      case (None, _) => logger.debug(s"Create relationship failed, node with id $node1 not found")
      case (_, None) => logger.debug(s"Create relationship failed, node with id $node2 not found")
    }
  }

  private def replaceNode(nodeId: NodeId, node: InternalNode): Unit = {
    buffer.indexedNodes.update(nodeId, node)
    buffer.nodes(node.label).update(nodeId, node)
  }

  def updateNode(nodeId: NodeId, props: Map[String, Any]): Unit = {
    if (buffer.indexedNodes.contains(nodeId)) {
      val node = buffer.indexedNodes(nodeId)
      replaceNode(nodeId, node.updateProps(props))
    }
  }

  def deleteNode(nodeId: NodeId): Unit = {

    if (buffer.indexedNodes.contains(nodeId)) {
      val node = buffer.indexedNodes(nodeId)

      val relationsToDelete = node.fetchIncoming
      relationsToDelete.foreach(kv => {
        val label = kv._1
        val nodeIds = kv._2

        nodeIds
          .map(buffer.indexedNodes(_))
          .foreach(node => {
            val updatedNode = node.removeRelation(label, nodeId)
            replaceNode(updatedNode.id, updatedNode)
          })
      })

      buffer.nodes(node.label).remove(node.id)
      buffer.indexedNodes.remove(nodeId)
    }
  }

  def deleteNodes(label: String, props: Map[String, Any]): Unit = {
    val matchingNodes = filterNodesByMatchingParams(label, props)

    matchingNodes.foreach(node => deleteNode(node.id))
  }

  def deleteRelationship(from: NodeId, label: String, to: NodeId): Unit = {
    val nodeFrom = buffer.indexedNodes.get(from)
    val nodeTo = buffer.indexedNodes.get(to)

    (nodeFrom, nodeTo) match {
      case (Some(_from), Some(_)) =>
        val updatedNode = _from.deleteRelationship(label, to)
        replaceNode(updatedNode.id, updatedNode)
      case (None, _) => logger.debug(s"Node with id $from does not exist")
      case (_, None) => logger.debug(s"Node with id $to does not exist")
    }
  }

  def clearAll(): Unit = {
    buffer.nodes.clear()
    buffer.indexedNodes.clear()
  }
  def batchImportNodes(batchOfNodes: IterableOnce[CsvNode], refToIdMapping: RefToIdMapping): Unit = {
    batchOfNodes.iterator
      .foreach(node => {
        if (!refToIdMapping.hasReference(node.uniqueRef, node.label)) {
          val nodeId = createNode(node.label, node.params)
          refToIdMapping.addMapping(node.label, node.uniqueRef, nodeId)
        }
      })
  }

  def batchImportRelations(relations: IterableOnce[Relation], refToIdMapping: RefToIdMapping): Unit = {
    relations.iterator
      .foreach(relation => {
        val fromLabel = relation.fromLabel
        val toLabel = relation.toLabel
        val fromId: NodeId = refToIdMapping.getFor(fromLabel, relation.fromRef).get
        val toId: NodeId = refToIdMapping.getFor(toLabel, relation.toRef).get
        createRelationship(fromId, relation.relation, toId)
      })
  }

  def createNode(label: String, props: Map[String, Any]): NodeId = {
    val id = idGenerator.generateId
    val hm = immutable.HashMap.empty[String, Any].concat(props)
    val node = InternalNode(label, id, hm)

    if (buffer.nodes.contains(label)) {
      buffer.nodes(label).put(node.id, node)
    } else {
      val hm = emptyNode()
      hm.put(node.id, node)
      buffer.nodes.put(label, hm)
    }

    buffer.indexedNodes.put(node.id, node)
    id
  }

  private def filterNodesByMatchingParams(label: String, params: Map[String, Any]) = {
    buffer
      .nodes(label)
      .values
      .filter(node => {
        params
          .map(kv => {
            val value = node.fetchParam(kv._1)
            value.isDefined && value.get == kv._2
          })
          .reduce((a, b) => a && b)
      })
  }
}

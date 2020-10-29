package com.bharatsim.engine.graph.custom

import com.bharatsim.engine.graph.GraphProvider.NodeId
import com.bharatsim.engine.graph.{GraphData, GraphNode, GraphProvider}
import com.github.tototoshi.csv.CSVReader
import com.typesafe.scalalogging.LazyLogging

import scala.collection.mutable

class GraphProviderImpl extends GraphProvider with LazyLogging {

  import GraphProviderImpl.idGenerator

  private val nodes: mutable.HashMap[String, mutable.HashMap[NodeId, InternalNode]] = mutable.HashMap.empty

  private val indexedNodes: mutable.HashMap[NodeId, InternalNode] = mutable.HashMap.empty

  override def ingestFromCsv(csvPath: String, mapper: Option[Function[Map[String, String], GraphData]]): Unit = {
    val reader = CSVReader.open(csvPath)
    val records = reader.allWithHeaders()
    val refToIdMappingBucket = mutable.HashMap[String, mutable.HashMap[Int, NodeId]]().empty

    if (mapper.isDefined) {
      records.foreach(record => {
        mapper.get(record).nodes.foreach(node => {
          if (!referenceAlreadyEncountered(node.uniqueRef, refToIdMappingBucket.get(node.label))) {
            val nodeId = createNode(node.label, node.params)
            if (!refToIdMappingBucket.contains(node.label))
              refToIdMappingBucket.put(node.label, new mutable.HashMap[Int, NodeId]())
            refToIdMappingBucket(node.label).put(node.uniqueRef, nodeId)
          }
        })

        mapper.get(record).relations.foreach(relation => {
          val fromLabel = relation.fromLabel
          val toLabel = relation.toLabel
          val fromId: NodeId = refToIdMappingBucket(fromLabel)(relation.refFrom)
          val toId: NodeId = refToIdMappingBucket(toLabel)(relation.refTo)
          createRelationship(fromId, relation.relation, toId)
        })
      })
    }
  }

  private def referenceAlreadyEncountered(uniqueRef: NodeId, refToIdMapping: Option[mutable.HashMap[NodeId, NodeId]]): Boolean = {
    if (refToIdMapping.isEmpty) false
    else {
      if (refToIdMapping.get.contains(uniqueRef)) true
      else false
    }
  }

  private[engine] override def createNode(label: String, props: (String, Any)*): NodeId = createNode(label, props.toMap)

  override def createRelationship(node1: NodeId, label: String, node2: NodeId): Unit = {
    val nodeFrom = indexedNodes.get(node1)
    val nodeTo = indexedNodes.get(node2)

    (nodeFrom, nodeTo) match {
      case (Some(from), Some(to)) => from.addRelation(label, to.id)
      case (None, _) => logger.debug(s"Create relationship failed, node with id $node1 not found")
      case (_, None) => logger.debug(s"Create relationship failed, node with id $node2 not found")
    }
  }

  private[engine] override def createNode(label: String, props: Map[String, Any]): NodeId = {
    val id = idGenerator.generateId
    val hm = new mutable.HashMap[String, Any]()
    hm.addAll(props)
    val node = InternalNode(label, id, hm)

    if (nodes.contains(label)) {
      nodes(label).put(node.id, node)
    } else {
      val hm = new mutable.HashMap[NodeId, InternalNode]()
      hm.put(node.id, node)
      nodes.put(label, hm)
    }

    indexedNodes.put(node.id, node)
    id
  }

  override def fetchNode(label: String, params: Map[String, Any] = Map.empty): Option[GraphNode] = {
    if (params.isEmpty) {
      if (nodes.contains(label) && nodes(label).nonEmpty) {
        Some(nodes(label).head._2.toGraphNode)
      } else None
    } else {
      if (nodes.contains(label) && nodes(label).nonEmpty) {
        val list = filterNodesByMatchingParams(label, params)
        if (list.nonEmpty) Some(list.head.toGraphNode)
        else None
      } else None
    }
  }

  override def fetchNodes(label: String, params: Map[String, Any]): Iterable[GraphNode] = {
    if (params.isEmpty) {
      if (nodes.contains(label) && nodes(label).nonEmpty) nodes(label).values.map(_.toGraphNode)
      else List.empty
    } else {
      if (nodes.contains(label) && nodes(label).nonEmpty) {
        filterNodesByMatchingParams(label, params).map(_.toGraphNode)
      } else List.empty
    }
  }

  override def fetchNodes(label: String, params: (String, Any)*): Iterable[GraphNode] = fetchNodes(label, params.toMap)

  override def fetchNeighborsOf(nodeId: NodeId, label: String, labels: String*): Iterable[GraphNode] = {
    val allLabels = label :: labels.toList

    if (indexedNodes.contains(nodeId)) {
      val node = indexedNodes(nodeId)
      allLabels
        .map(l => node.fetchNeighborsWithLabel(l))
        .foldLeft(new mutable.HashSet[NodeId]())((acc, mp) => acc ++ mp)
        .map(indexedNodes(_))
        .map(_.toGraphNode)
    } else {
      logger.debug(s"Node with id $nodeId does not exist")
      Iterable.empty
    }
  }

  override def neighborCount(nodeId: NodeId, label: String, matchCondition: (String, Any)): Int = {
    if (indexedNodes.contains(nodeId)) {
      val node = indexedNodes(nodeId)
      var count = 0
      node.fetchNeighborsWithLabel(label)
        .foreach(nodeId => {
          val n = indexedNodes(nodeId)
          n.params.get(matchCondition._1) match {
            case Some(v) => if (v == matchCondition._2) count += 1
            case _ =>
          }
        })
      count
    } else 0
  }

  override def updateNode(nodeId: NodeId, props: Map[String, Any]): Unit = {
    if (indexedNodes.contains(nodeId)) {
      val node = indexedNodes(nodeId)
      node.updateProps(props)
    }
  }

  override def updateNode(nodeId: NodeId, prop: (String, Any), props: (String, Any)*): Unit =
    updateNode(nodeId, (prop :: props.toList).toMap)

  override def deleteNode(nodeId: NodeId): Unit = {
    if (indexedNodes.contains(nodeId)) {
      val node = indexedNodes(nodeId)
      nodes(node.label).remove(node.id)
      indexedNodes.remove(nodeId)
    }
  }

  override def deleteNodes(label: String, props: Map[String, Any]): Unit = {
    val matchingNodes = filterNodesByMatchingParams(label, props)

    matchingNodes.foreach(node => {
      indexedNodes.remove(node.id)
      nodes(label).remove(node.id)
    })
  }

  override def deleteRelationship(from: NodeId, label: String, to: NodeId): Unit = {
    val nodeFrom = indexedNodes.get(from)
    val nodeTo = indexedNodes.get(to)

    (nodeFrom, nodeTo) match {
      case (Some(_from), Some(_)) => _from.deleteRelationship(label, to)
      case (None, _)              => logger.debug(s"Node with id $from does not exist")
      case (_, None)              => logger.debug(s"Node with id $to does not exist")
    }
  }

  override def deleteAll(): Unit = {
    nodes.clear()
    indexedNodes.clear()
  }

  private def filterNodesByMatchingParams(label: String, params: Map[String, Any]) = {
    nodes(label).values.filter(node => {
      params
        .map(kv => {
          val value = node.fetchParam(kv._1)
          value.isDefined && value.get == kv._2
        })
        .reduce((a, b) => a && b)
    })
  }

  override def shutdown(): Unit = {}
}

object GraphProviderImpl {
  private val idGenerator = new IdGenerator
}

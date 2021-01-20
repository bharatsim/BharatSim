package com.bharatsim.engine.graph.custom
import com.bharatsim.engine.graph.GraphNode
import com.bharatsim.engine.graph.GraphProvider.NodeId
import com.bharatsim.engine.graph.ingestion.{CsvNode, GraphData, RefToIdMapping, Relation}
import com.bharatsim.engine.graph.patternMatcher.MatchPattern
import com.github.tototoshi.csv.CSVReader
import com.typesafe.scalalogging.LazyLogging

import scala.collection.{immutable, mutable}

private[custom] object GraphOperations extends LazyLogging {
  private val idGenerator = new IdGenerator
  type NodesType = mutable.Map[String, mutable.Map[NodeId, InternalNode]]
  type IndexedNodesType = mutable.Map[NodeId, InternalNode]

  def ingestFromCsv(
      nodes: NodesType,
      indexedNodes: IndexedNodesType,
      emptyNode: () => mutable.Map[NodeId, InternalNode],
      csvPath: String,
      mapper: Option[Function[Map[String, String], GraphData]]
  ): Unit = {
    val reader = CSVReader.open(csvPath)
    val records = reader.allWithHeaders()

    if (mapper.isDefined) {
      val batchOfNodes = mutable.ListBuffer.empty[CsvNode]
      val batchOfRelations = mutable.ListBuffer.empty[Relation]
      records.foreach(row => {
        val graphData = mapper.get.apply(row)
        batchOfNodes.addAll(graphData._nodes)
        batchOfRelations.addAll(graphData._relations)
      })

      val refToIdMapping = batchImportNodes(nodes, indexedNodes, emptyNode, batchOfNodes)
      batchImportRelations(nodes, indexedNodes, batchOfRelations, refToIdMapping)
    }
  }

  def createRelationship(
      nodes: NodesType,
      indexedNodes: IndexedNodesType,
      node1: NodeId,
      label: String,
      node2: NodeId
  ): Unit = {
    val nodeFrom = indexedNodes.get(node1)
    val nodeTo = indexedNodes.get(node2)

    (nodeFrom, nodeTo) match {
      case (Some(from), Some(to)) => {
        val updatedFromNode = from.addRelation(label, to.id)
        val updatedToNode = to.addIncoming(label, from.id)
        replaceNode(nodes, indexedNodes, updatedFromNode.id, updatedFromNode)
        replaceNode(nodes, indexedNodes, updatedToNode.id, updatedToNode)

      }
      case (None, _) => logger.debug(s"Create relationship failed, node with id $node1 not found")
      case (_, None) => logger.debug(s"Create relationship failed, node with id $node2 not found")
    }
  }

  private[engine] def createNode(
      nodes: NodesType,
      indexedNodes: IndexedNodesType,
      emptyNode: () => mutable.Map[NodeId, InternalNode],
      label: String,
      props: Map[String, Any]
  ): NodeId = {
    val id = idGenerator.generateId
    val hm = immutable.HashMap.empty[String, Any].concat(props)
    val node = InternalNode(label, id, hm)

    if (nodes.contains(label)) {
      nodes(label).put(node.id, node)
    } else {
      val hm = emptyNode()
      hm.put(node.id, node)
      nodes.put(label, hm)
    }

    indexedNodes.put(node.id, node)
    id
  }

  def fetchNode(nodes: NodesType, label: String, params: Map[String, Any] = Map.empty): Option[GraphNode] = {
    if (params.isEmpty) {
      if (nodes.contains(label) && nodes(label).nonEmpty) {
        Some(nodes(label).head._2.toGraphNode)
      } else None
    } else {
      if (nodes.contains(label) && nodes(label).nonEmpty) {
        val list = filterNodesByMatchingParams(nodes, label, params)
        if (list.nonEmpty) Some(list.head.toGraphNode)
        else None
      } else None
    }
  }

  def fetchNodes(nodes: NodesType, label: String, params: Map[String, Any]): Iterable[GraphNode] = {
    if (params.isEmpty) {
      if (nodes.contains(label) && nodes(label).nonEmpty) nodes(label).values.map(_.toGraphNode)
      else List.empty
    } else {
      if (nodes.contains(label) && nodes(label).nonEmpty) {
        filterNodesByMatchingParams(nodes, label, params).map(_.toGraphNode)
      } else List.empty
    }
  }

  def fetchNodes(nodes: NodesType, label: String, matchPattern: MatchPattern): Iterable[GraphNode] = {
    if (nodes.contains(label) && nodes(label).nonEmpty) {
      nodes(label).values.filter(node => matchPattern.eval(node.params.toMap)).map(_.toGraphNode)
    } else List.empty
  }

  def fetchCount(nodes: NodesType, label: String, matchPattern: MatchPattern): Int = {
    nodes
      .getOrElse(label, List.empty)
      .count(nodesForLable => matchPattern.eval(nodesForLable._2.params.toMap))
  }

  def fetchNeighborsOf(
      indexedNodes: IndexedNodesType,
      nodeId: NodeId,
      allLabels: List[String]
  ): Iterable[GraphNode] = {

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

  def neighborCount(
      indexedNodes: IndexedNodesType,
      nodeId: NodeId,
      label: String,
      matchCondition: MatchPattern
  ): Int = {

    if (indexedNodes.contains(nodeId)) {
      val node = indexedNodes(nodeId)
      var count = 0
      node
        .fetchNeighborsWithLabel(label)
        .foreach(nodeId => {
          val n = indexedNodes(nodeId)
          if (matchCondition.eval(n.params.toMap)) count += 1
        })
      count
    } else 0
  }

  private def replaceNode(
      nodes: NodesType,
      indexedNodes: IndexedNodesType,
      nodeId: NodeId,
      node: InternalNode
  ): Unit = {
    indexedNodes.update(nodeId, node)
    nodes(node.label).update(nodeId, node)
  }

  def updateNode(nodes: NodesType, indexedNodes: IndexedNodesType, nodeId: NodeId, props: Map[String, Any]): Unit = {
    if (indexedNodes.contains(nodeId)) {
      val node = indexedNodes(nodeId)
      replaceNode(nodes, indexedNodes, nodeId, node.updateProps(props))
    }
  }

  def deleteNode(nodes: NodesType, indexedNodes: IndexedNodesType, nodeId: NodeId): Unit = {

    if (indexedNodes.contains(nodeId)) {
      val node = indexedNodes(nodeId)

      val relationsToDelete = node.fetchIncoming
      relationsToDelete.foreach(kv => {
        val label = kv._1
        val nodeIds = kv._2

        nodeIds
          .map(indexedNodes(_))
          .foreach(node => {
            val updatedNode = node.removeRelation(label, nodeId)
            replaceNode(nodes, indexedNodes, updatedNode.id, updatedNode)
          })
      })

      nodes(node.label).remove(node.id)
      indexedNodes.remove(nodeId)
    }
  }

  def deleteNodes(nodes: NodesType, indexedNodes: IndexedNodesType, label: String, props: Map[String, Any]): Unit = {
    val matchingNodes = filterNodesByMatchingParams(nodes, label, props)

    matchingNodes.foreach(node => {
      deleteNode(nodes, indexedNodes, node.id)
    })
  }

  def deleteRelationship(
      nodes: NodesType,
      indexedNodes: IndexedNodesType,
      from: NodeId,
      label: String,
      to: NodeId
  ): Unit = {
    val nodeFrom = indexedNodes.get(from)
    val nodeTo = indexedNodes.get(to)

    (nodeFrom, nodeTo) match {
      case (Some(_from), Some(_)) => {
        val updatedNode = _from.deleteRelationship(label, to);
        replaceNode(nodes, indexedNodes, updatedNode.id, updatedNode)
      }
      case (None, _) => logger.debug(s"Node with id $from does not exist")
      case (_, None) => logger.debug(s"Node with id $to does not exist")
    }
  }

  def deleteAll(nodes: NodesType, indexedNodes: IndexedNodesType): Unit = {
    nodes.clear()
    indexedNodes.clear()
  }

  private def filterNodesByMatchingParams(
      nodes: NodesType,
      label: String,
      params: Map[String, Any]
  ): Iterable[InternalNode] = {

    nodes(label).values.filter(node => {
      params
        .map(kv => {
          val value = node.fetchParam(kv._1)
          value.isDefined && value.get == kv._2
        })
        .reduce((a, b) => a && b)
    })
  }

  def batchImportNodes(
      nodes: NodesType,
      indexedNodes: IndexedNodesType,
      emptyNode: () => mutable.Map[NodeId, InternalNode],
      batchOfNodes: IterableOnce[CsvNode]
  ): RefToIdMapping = {
    val refToIdMapping = new RefToIdMapping
    batchOfNodes.iterator
      .foreach(node => {
        if (!refToIdMapping.hasReference(node.uniqueRef, node.label)) {
          val nodeId = createNode(nodes, indexedNodes, emptyNode, node.label, node.params)
          refToIdMapping.addMapping(node.label, node.uniqueRef, nodeId)
        }
      })

    refToIdMapping
  }

  def batchImportRelations(
      nodes: NodesType,
      indexedNodes: IndexedNodesType,
      relations: IterableOnce[Relation],
      refToIdMapping: RefToIdMapping
  ): Unit = {
    relations.iterator
      .foreach(relation => {
        val fromLabel = relation.fromLabel
        val toLabel = relation.toLabel
        val fromId: NodeId = refToIdMapping.getFor(fromLabel, relation.fromRef).get
        val toId: NodeId = refToIdMapping.getFor(toLabel, relation.toRef).get
        createRelationship(nodes, indexedNodes, fromId, relation.relation, toId)
      })
  }

}

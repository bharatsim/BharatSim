package com.bharatsim.engine.graph.custom

import com.bharatsim.engine.graph.GraphProvider.NodeId
import com.bharatsim.engine.graph.ingestion.{CsvNode, GraphData, RefToIdMapping, Relation}
import com.bharatsim.engine.graph.patternMatcher.MatchPattern
import com.bharatsim.engine.graph.{GraphNode, GraphProvider}
import com.typesafe.scalalogging.LazyLogging

import scala.collection.mutable

private[engine] class GraphProviderImpl extends GraphProvider with LazyLogging {

  type NodesType = mutable.HashMap[String, mutable.HashMap[NodeId, InternalNode]]
  type IndexedNodesType = mutable.HashMap[NodeId, InternalNode]
  private val nodes: NodesType = mutable.HashMap.empty
  private val indexedNodes: IndexedNodesType = mutable.HashMap.empty

  private def emptyNode = () => new mutable.HashMap[NodeId, InternalNode]()
  override def ingestFromCsv(csvPath: String, mapper: Option[Function[Map[String, String], GraphData]]): Unit = {
    GraphOperations.ingestFromCsv(
      nodes.asInstanceOf[GraphOperations.NodesType],
      indexedNodes,
      emptyNode,
      csvPath,
      mapper
    )
  }

  private[engine] override def createNode(label: String, props: (String, Any)*): NodeId = createNode(label, props.toMap)

  override def createRelationship(node1: NodeId, label: String, node2: NodeId): Unit = {
    GraphOperations.createRelationship(
      nodes.asInstanceOf[GraphOperations.NodesType],
      indexedNodes,
      node1,
      label,
      node2
    )
  }

  private[engine] override def createNode(label: String, props: Map[String, Any]): NodeId = {
    GraphOperations.createNode(
      nodes.asInstanceOf[GraphOperations.NodesType],
      indexedNodes,
      emptyNode,
      label,
      props
    )
  }

  override def fetchNode(label: String, params: Map[String, Any] = Map.empty): Option[GraphNode] = {
    GraphOperations.fetchNode(nodes.asInstanceOf[GraphOperations.NodesType], label, params)
  }

  override def fetchNodes(label: String, params: Map[String, Any]): Iterable[GraphNode] = {
    GraphOperations.fetchNodes(nodes.asInstanceOf[GraphOperations.NodesType], label, params)
  }

  override def fetchNodes(label: String, params: (String, Any)*): Iterable[GraphNode] = fetchNodes(label, params.toMap)

  override def fetchCount(label: String, matchPattern: MatchPattern): Int = {
    GraphOperations.fetchCount(nodes.asInstanceOf[GraphOperations.NodesType], label, matchPattern)
  }

  override def fetchNeighborsOf(nodeId: NodeId, label: String, labels: String*): Iterable[GraphNode] = {
    GraphOperations.fetchNeighborsOf(indexedNodes, nodeId, label :: labels.toList)
  }

  override def neighborCount(nodeId: NodeId, label: String, matchCondition: MatchPattern): Int = {
    GraphOperations.neighborCount(indexedNodes, nodeId, label, matchCondition)
  }

  override def updateNode(nodeId: NodeId, props: Map[String, Any]): Unit = {
    GraphOperations.updateNode(
      nodes.asInstanceOf[GraphOperations.NodesType],
      indexedNodes,
      nodeId,
      props
    )
  }

  override def updateNode(nodeId: NodeId, prop: (String, Any), props: (String, Any)*): Unit =
    updateNode(nodeId, (prop :: props.toList).toMap)

  override def deleteNode(nodeId: NodeId): Unit = {
    GraphOperations.deleteNode(nodes.asInstanceOf[GraphOperations.NodesType], indexedNodes, nodeId)
  }

  override def deleteNodes(label: String, props: Map[String, Any]): Unit = {
    GraphOperations.deleteNodes(nodes.asInstanceOf[GraphOperations.NodesType], indexedNodes, label, props)
  }

  override def deleteRelationship(from: NodeId, label: String, to: NodeId): Unit = {
    GraphOperations.deleteRelationship(
      nodes.asInstanceOf[GraphOperations.NodesType],
      indexedNodes,
      from,
      label,
      to
    )
  }

  override def deleteAll(): Unit = {
    GraphOperations.deleteAll(nodes.asInstanceOf[GraphOperations.NodesType], indexedNodes)

  }

  override def shutdown(): Unit = {}

  override private[engine] def batchImportNodes(batchOfNodes: IterableOnce[CsvNode]): RefToIdMapping = {
    GraphOperations.batchImportNodes(
      nodes.asInstanceOf[GraphOperations.NodesType],
      indexedNodes,
      emptyNode,
      batchOfNodes
    )
  }

  override private[engine] def batchImportRelations(
      relations: IterableOnce[Relation],
      refToIdMapping: RefToIdMapping
  ): Unit = {

    GraphOperations.batchImportRelations(
      nodes.asInstanceOf[GraphOperations.NodesType],
      indexedNodes,
      relations,
      refToIdMapping
    )

  }

}

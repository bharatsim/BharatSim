package com.bharatsim.engine.graph.custom

import com.bharatsim.engine.Context
import com.bharatsim.engine.graph.GraphProvider.NodeId
import com.bharatsim.engine.graph.ingestion.{CsvNode, GraphData, RefToIdMapping, Relation}
import com.bharatsim.engine.graph.patternMatcher.MatchPattern
import com.bharatsim.engine.graph.{GraphNode, GraphProvider}
import com.bharatsim.engine.listeners.{SimulationListener, SimulationListenerRegistry}

import scala.collection.concurrent.TrieMap

private[engine] class GraphProviderWithBufferImpl extends GraphProvider with SimulationListener {

  SimulationListenerRegistry.register(this)
  type NodesBufferType = TrieMap[String, TrieMap[NodeId, InternalNode]]
  type IndexedNodesBufferType = TrieMap[NodeId, InternalNode]

  private var nodesReadBuffer: NodesBufferType = TrieMap.empty
  private var indexedNodesReadBuffer: IndexedNodesBufferType = TrieMap.empty

  private val nodesWriteBuffer: NodesBufferType = TrieMap.empty
  private val indexedNodesWriteBuffer: IndexedNodesBufferType = TrieMap.empty

  private def emptyNode = () => new TrieMap[NodeId, InternalNode]()
  override def ingestFromCsv(csvPath: String, mapper: Option[Function[Map[String, String], GraphData]]): Unit = {
    GraphOperations.ingestFromCsv(
      nodesWriteBuffer.asInstanceOf[GraphOperations.NodesType],
      indexedNodesWriteBuffer,
      emptyNode,
      csvPath,
      mapper
    )
    syncBuffers()
  }

  private[engine] override def createNode(label: String, props: (String, Any)*): NodeId = createNode(label, props.toMap)

  override def createRelationship(node1: NodeId, label: String, node2: NodeId): Unit = {
    GraphOperations.createRelationship(
      nodesWriteBuffer.asInstanceOf[GraphOperations.NodesType],
      indexedNodesWriteBuffer,
      node1,
      label,
      node2
    )
  }

  private[engine] override def createNode(label: String, props: Map[String, Any]): NodeId = {
    GraphOperations.createNode(
      nodesWriteBuffer.asInstanceOf[GraphOperations.NodesType],
      indexedNodesWriteBuffer,
      emptyNode,
      label,
      props
    )
  }

  override def fetchNode(label: String, params: Map[String, Any] = Map.empty): Option[GraphNode] = {
    GraphOperations.fetchNode(nodesReadBuffer.asInstanceOf[GraphOperations.NodesType], label, params)
  }

  override def fetchNodes(label: String, params: Map[String, Any]): Iterable[GraphNode] = {
    GraphOperations.fetchNodes(nodesReadBuffer.asInstanceOf[GraphOperations.NodesType], label, params)
  }

  override def fetchNodes(label: String, params: (String, Any)*): Iterable[GraphNode] = fetchNodes(label, params.toMap)

  override def fetchCount(label: String, matchPattern: MatchPattern): Int = {
    GraphOperations.fetchCount(nodesReadBuffer.asInstanceOf[GraphOperations.NodesType], label, matchPattern)
  }

  override def fetchNeighborsOf(nodeId: NodeId, label: String, labels: String*): Iterable[GraphNode] = {
    GraphOperations.fetchNeighborsOf(indexedNodesReadBuffer, nodeId, label :: labels.toList)
  }

  override def neighborCount(nodeId: NodeId, label: String, matchCondition: MatchPattern): Int = {
    GraphOperations.neighborCount(indexedNodesReadBuffer, nodeId, label, matchCondition)
  }

  override def updateNode(nodeId: NodeId, props: Map[String, Any]): Unit = {
    GraphOperations.updateNode(
      nodesWriteBuffer.asInstanceOf[GraphOperations.NodesType],
      indexedNodesWriteBuffer,
      nodeId,
      props
    )
  }

  override def updateNode(nodeId: NodeId, prop: (String, Any), props: (String, Any)*): Unit =
    updateNode(nodeId, (prop :: props.toList).toMap)

  override def deleteNode(nodeId: NodeId): Unit = {
    val nodes = nodesWriteBuffer.asInstanceOf[GraphOperations.NodesType];
    GraphOperations.deleteNode(nodes, indexedNodesWriteBuffer, nodeId)
  }

  override def deleteNodes(label: String, props: Map[String, Any]): Unit = {
    val nodes = nodesWriteBuffer.asInstanceOf[GraphOperations.NodesType];
    GraphOperations.deleteNodes(nodes, indexedNodesWriteBuffer, label, props)
  }

  override def deleteRelationship(from: NodeId, label: String, to: NodeId): Unit = {
    val nodes = nodesWriteBuffer.asInstanceOf[GraphOperations.NodesType];
    GraphOperations.deleteRelationship(nodes, indexedNodesWriteBuffer, from, label, to)
  }

  override def deleteAll(): Unit = {
    val nodes = nodesWriteBuffer.asInstanceOf[GraphOperations.NodesType];
    GraphOperations.deleteAll(nodes, indexedNodesWriteBuffer)
  }

  override def shutdown(): Unit = {}

  override private[engine] def batchImportNodes(batchOfNodes: IterableOnce[CsvNode]): RefToIdMapping = {
    val nodes = nodesWriteBuffer.asInstanceOf[GraphOperations.NodesType];
    GraphOperations.batchImportNodes(nodes, indexedNodesWriteBuffer, emptyNode, batchOfNodes)
  }

  override private[engine] def batchImportRelations(
      relations: IterableOnce[Relation],
      refToIdMapping: RefToIdMapping
  ): Unit = {
    val nodes = nodesWriteBuffer.asInstanceOf[GraphOperations.NodesType];

    GraphOperations.batchImportRelations(nodes, indexedNodesWriteBuffer, relations, refToIdMapping)

  }

  def syncBuffers(): Unit = {
    nodesReadBuffer = nodesWriteBuffer.map((kv) => (kv._1, kv._2.snapshot()));
    indexedNodesReadBuffer = indexedNodesWriteBuffer.snapshot();
  }

  override def onSimulationStart(context: Context): Unit = {}
  override def onStepStart(context: Context): Unit = {
    syncBuffers()
  }

  override def onStepEnd(context: Context): Unit = {}
  override def onSimulationEnd(context: Context): Unit = {}
}

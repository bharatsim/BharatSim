package com.bharatsim.engine.graph.custom

import com.bharatsim.engine.graph.GraphProvider.NodeId
import com.bharatsim.engine.graph.custom.GraphOperations.{IndexedNodesType, NodesType}
import com.bharatsim.engine.graph.ingestion.{CsvNode, GraphData, RefToIdMapping, Relation}
import com.bharatsim.engine.graph.patternMatcher.MatchPattern
import com.bharatsim.engine.graph.{GraphNode, GraphProvider}
import com.typesafe.scalalogging.LazyLogging

import scala.collection.mutable

private[engine] class GraphProviderImpl(graphOperations: GraphOperations) extends GraphProvider with LazyLogging {
  override def createRelationship(node1: NodeId, label: String, node2: NodeId): Unit = {
    graphOperations.writeOperations.createRelationship(node1, label, node2)
  }

  private[engine] override def createNode(label: String, props: Map[String, Any]): NodeId = {
    graphOperations.writeOperations.createNode(label, props)
  }

  override def fetchNode(label: String, params: Map[String, Any] = Map.empty): Option[GraphNode] = {
    graphOperations.readOperations.fetchNode(label, params)
  }

  override def fetchNodes(label: String, params: Map[String, Any]): Iterable[GraphNode] = {
    graphOperations.readOperations.fetchNodes(label, params)
  }

  override def fetchNodes(label: String, matchPattern: MatchPattern): Iterable[GraphNode] = {
    graphOperations.readOperations.fetchNodes(label, matchPattern)
  }

  override def fetchCount(label: String, matchPattern: MatchPattern): Int = {
    graphOperations.readOperations.fetchCount(label, matchPattern)
  }

  override def fetchNeighborsOf(nodeId: NodeId, label: String, labels: String*): Iterable[GraphNode] = {
    graphOperations.readOperations.fetchNeighborsOf(nodeId, label :: labels.toList)
  }

  override def neighborCount(nodeId: NodeId, label: String, matchCondition: MatchPattern): Int = {
    graphOperations.readOperations.neighborCount(nodeId, label, matchCondition)
  }

  override def updateNode(nodeId: NodeId, props: Map[String, Any]): Unit = {
    graphOperations.writeOperations.updateNode(nodeId, props)
  }

  override def deleteNode(nodeId: NodeId): Unit = {
    graphOperations.writeOperations.deleteNode(nodeId)
  }

  override def deleteNodes(label: String, props: Map[String, Any]): Unit = {
    graphOperations.writeOperations.deleteNodes(label, props)
  }

  override def deleteRelationship(from: NodeId, label: String, to: NodeId): Unit = {
    graphOperations.writeOperations.deleteRelationship(from, label, to)
  }

  override def deleteAll(): Unit = graphOperations.deleteAll()

  override def shutdown(): Unit = {}

  override private[engine] def batchImportNodes(
      batchOfNodes: IterableOnce[CsvNode],
      refToIdMapping: RefToIdMapping
  ): Unit = {
    graphOperations.writeOperations.batchImportNodes(batchOfNodes, refToIdMapping)
  }

  override private[engine] def batchImportRelations(
      relations: IterableOnce[Relation],
      refToIdMapping: RefToIdMapping
  ): Unit = {
    graphOperations.writeOperations.batchImportRelations(relations, refToIdMapping)
  }
}

object GraphProviderImpl {
  def apply(): GraphProviderImpl = {
    val nodes: NodesType = mutable.HashMap.empty
    val indexedNodes: IndexedNodesType = mutable.HashMap.empty
    val idGenerator = new IdGenerator
    new GraphProviderImpl(new Graph(Buffer(nodes, indexedNodes), idGenerator))
  }
}

package com.bharatsim.engine.graph
import java.nio.file.Path

import com.bharatsim.engine.graph.GraphProvider.NodeId

class GraphProviderImpl extends GraphProvider {
  override def createNode(label: String, props: Map[String, Any]): NodeId = ???

  override def createRelationship(label: String, node1: NodeId, node2: NodeId): Unit = ???

  override def ingestNodes(csvPath: Path): Unit = ???

  override def ingestRelationships(csvPath: Path): Unit = ???

  override def fetchNode(label: String, params: Map[String, Any]): Some[GraphNode] = ???

  override def fetchNodes(label: String, params: Map[String, Any]): Iterable[GraphNode] = ???

  override def fetchNeighborsOf(nodeId: NodeId, labels: String*): Iterable[GraphNode] = ???

  override def updateNode(nodeId: NodeId, props: Map[String, Any]): Unit = ???

  override def deleteNode(nodeId: NodeId): Unit = ???

  override def deleteRelationship(label: String, from: NodeId, to: NodeId): Unit = ???

  override def deleteNodes(props: Map[String, Any]): Unit = ???

  override def deleteAll(): Unit = ???

  override def createNode(label: String, props: (String, Any)*): NodeId = ???

  override def fetchNodes(label: String, params: (String, Any)*): Iterable[GraphNode] = ???

  override def updateNode(nodeId: NodeId, props: (String, Any)*): Unit = ???
}

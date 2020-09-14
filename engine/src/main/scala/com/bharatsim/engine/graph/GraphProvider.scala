package com.bharatsim.engine.graph

import java.nio.file.Path

import com.bharatsim.engine.graph.GraphProvider.NodeId

trait GraphNode {
  def label: String

  def Id: NodeId

  def apply(key: String): Option[Any]

  def getParams: Map[String, Any]
}

trait GraphProvider {
  /* CRUD */

  // C
  def createNode(label: String, props: Map[String, Any]): NodeId

  def createNode(label: String, props: (String, Any)*): NodeId

  def createRelationship(node1: NodeId, label: String, node2: NodeId): Unit

  def ingestNodes(csvPath: Path): Unit

  def ingestRelationships(csvPath: Path): Unit

  // R
  def fetchNode(label: String, params: Map[String, Any]): Option[GraphNode]

  def fetchNodes(label: String, params: Map[String, Any]): Iterable[GraphNode]

  def fetchNodes(label: String, params: (String, Any)*): Iterable[GraphNode]

  def fetchNeighborsOf(nodeId: NodeId, label: String, labels: String*): Iterable[GraphNode]

  // U
  def updateNode(nodeId: NodeId, props: Map[String, Any]): Unit

  def updateNode(nodeId: NodeId, prop: (String, Any), props: (String, Any)*): Unit

  // D
  def deleteNode(nodeId: NodeId): Unit

  def deleteRelationship(from: NodeId, label: String, to: NodeId): Unit

  def deleteNodes(label: String, props: Map[String, Any])

  def deleteAll(): Unit
}

object GraphProvider {
  type NodeId = Int
}

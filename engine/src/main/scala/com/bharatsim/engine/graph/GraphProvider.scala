package com.bharatsim.engine.graph

import java.nio.file.Path

import com.bharatsim.engine.graph.GraphProvider.NodeId

trait Node {
  def Id: NodeId

  def apply(key: String): Option[Any]
}

trait GraphProvider {
  /* CRUD */

  // C
  def createNode(label: String, props: Map[String, Any]): NodeId

  def createRelationship(label: String, node1: NodeId, node2: NodeId)

  def ingestNodes(csvPath: Path): Unit

  def ingestRelationships(csvPath: Path): Unit

  // R
  def fetchNode(label: String, params: Map[String, Any]): Some[Node]

  def fetchNodes(label: String, params: Map[String, Any]): Iterable[Node]

  def fetchNeighborsOf(nodeId: NodeId, labels: String*): Iterable[Node]

  // U
  def updateNode(nodeId: NodeId, props: Map[String, Any]): Unit

  // D
  def deleteNode(nodeId: NodeId): Unit

  def deleteRelationship(label: String, from: NodeId, to: NodeId)

  def deleteNodes(props: Map[String, Any])
}

object GraphProvider {
  type NodeId = Int
}

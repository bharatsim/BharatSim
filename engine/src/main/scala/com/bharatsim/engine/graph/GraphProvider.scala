package com.bharatsim.engine.graph

import java.nio.file.Path

import com.bharatsim.engine.graph.GraphProvider.NodeId

trait DataNode {
  def label: String
  def Id: NodeId
  def getParams: Map[String, Any]
}

trait GraphNode extends DataNode {
  def apply(key: String): Option[Any]
}

case class Relation(from: DataNode, relation: String, to: DataNode)
case class GraphData(nodes: List[DataNode], relations: List[Relation])

trait GraphProvider {
  /* CRUD */

  // C
  def createNode(label: String, props: Map[String, Any]): NodeId

  def createNode(label: String, props: (String, Any)*): NodeId

  def createRelationship(node1: NodeId, label: String, node2: NodeId): Unit

  def ingestFromCsv(csvPath: String, mapper: Option[Function[Map[String, String], GraphData]]): Unit
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

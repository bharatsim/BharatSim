package com.bharatsim.engine.graph

import com.bharatsim.engine.Node
import com.bharatsim.engine.basicConversions.decoders.BasicMapDecoder
import com.bharatsim.engine.basicConversions.decoders.BasicMapDecoder.decodeMap
import com.bharatsim.engine.basicConversions.encoders.BasicMapEncoder
import com.bharatsim.engine.graph.GraphProvider.NodeId
import com.bharatsim.engine.utils.Utils.fetchClassName

import scala.collection.mutable.ListBuffer
import scala.reflect.ClassTag

trait CsvNode[T <: Node] {
  def node: T

  def label: String
}

trait DataNode {
  def label: String

  def Id: NodeId

  def getParams: Map[String, Any]
}

trait GraphNode extends DataNode {
  def apply(key: String): Option[Any]

  def as[T <: Node](implicit decoder: BasicMapDecoder[T]): T = {
    val node = decodeMap(getParams)
    node.setId(Id)
    node
  }
}

case class Relation(from: NodeId, relation: String, to: NodeId)

class GraphData() {
  private[engine] val relations: ListBuffer[Relation] = ListBuffer.empty
  private[engine] val nodes: ListBuffer[DataNode] = ListBuffer.empty

  def addNode[T <: Product : ClassTag](id: NodeId, n: T)(implicit encoder: BasicMapEncoder[T]): Unit = {
    nodes.addOne(new DataNode {
      override def label: String = fetchClassName[T]

      override def Id: NodeId = id

      override def getParams: Map[String, Any] = encoder.encode(n).toMap
    })
  }

  def addRelations(r: Iterable[Relation]): Unit = {
    relations.addAll(r)
  }
}

trait GraphProvider {
  /* CRUD */

  def createNodeFromInstance[T <: Product](label: String, x: T)(implicit encoder: BasicMapEncoder[T]): NodeId = {
    createNode(label, encoder.encode(x).m)
  }

  // C
  private[engine] def createNode(label: String, props: Map[String, Any]): NodeId

  private[engine] def createNode(label: String, props: (String, Any)*): NodeId

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

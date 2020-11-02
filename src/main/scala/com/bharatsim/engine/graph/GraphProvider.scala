package com.bharatsim.engine.graph

import com.bharatsim.engine.basicConversions.BasicConversions
import com.bharatsim.engine.basicConversions.decoders.BasicMapDecoder
import com.bharatsim.engine.basicConversions.encoders.BasicMapEncoder
import com.bharatsim.engine.graph.GraphProvider.NodeId
import com.bharatsim.engine.graph.Relation.GenericRelation
import com.bharatsim.engine.graph.patternMatcher.MatchPattern
import com.bharatsim.engine.models.Node
import com.bharatsim.engine.utils.Utils.fetchClassName

import scala.collection.mutable.ListBuffer
import scala.reflect.ClassTag

trait CsvNode {
  def label: String

  def uniqueRef: Int

  def params: Map[String, Any]
}

trait GraphNode {
  def label: String

  def Id: NodeId

  def getParams: Map[String, Any]

  def apply(key: String): Option[Any]

  def as[T <: Node](implicit decoder: BasicMapDecoder[T]): T = {
    val node = BasicConversions.decode(getParams)
    node.setId(Id)
    node
  }
}

case class Relation[F <: Node : ClassTag, T <: Node : ClassTag](refFrom: Int, relation: String, refTo: Int) {
  private[engine] def fromLabel: String = fetchClassName[F]

  private[engine] def toLabel: String = fetchClassName[T]
}

object Relation {
  type GenericRelation = Relation[_, _]
}

class GraphData() {
  private[engine] val relations: ListBuffer[GenericRelation] = ListBuffer.empty
  private[engine] val nodes: ListBuffer[CsvNode] = ListBuffer.empty

  def addNode[T <: Product : ClassTag](ref: Int, n: T)(implicit encoder: BasicMapEncoder[T]): Unit = {
    nodes.addOne(new CsvNode {
      override def label: String = fetchClassName[T]

      override def uniqueRef: NodeId = ref

      override def params: Map[String, Any] = encoder.encode(n).toMap
    })
  }

  def addRelations(r: Iterable[GenericRelation]): Unit = {
    relations.addAll(r)
  }
}

trait GraphProvider {
  /* CRUD */

  def createNodeFromInstance[T <: Product](label: String, x: T)(implicit encoder: BasicMapEncoder[T]): NodeId = {
    createNode(label, BasicConversions.encode(x))
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

  def fetchCount(label: String, matchPattern: MatchPattern): Int

  def fetchNeighborsOf(nodeId: NodeId, label: String, labels: String*): Iterable[GraphNode]

  def neighborCount(nodeId: NodeId, label: String, matchCondition: MatchPattern): Int

  // U
  def updateNode(nodeId: NodeId, props: Map[String, Any]): Unit

  def updateNode(nodeId: NodeId, prop: (String, Any), props: (String, Any)*): Unit

  // D
  def deleteNode(nodeId: NodeId): Unit

  def deleteRelationship(from: NodeId, label: String, to: NodeId): Unit

  def deleteNodes(label: String, props: Map[String, Any])

  def deleteAll(): Unit

  def shutdown(): Unit
}

object GraphProvider {
  type NodeId = Int
}

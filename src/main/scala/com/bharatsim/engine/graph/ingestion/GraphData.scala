package com.bharatsim.engine.graph.ingestion

import com.bharatsim.engine.basicConversions.encoders.BasicMapEncoder
import com.bharatsim.engine.graph.GraphProvider.NodeId
import com.bharatsim.engine.graph.ingestion.Relation.GenericRelation
import com.bharatsim.engine.models.Node
import com.bharatsim.engine.utils.Utils.fetchClassName

import scala.collection.mutable.ListBuffer
import scala.reflect.ClassTag

class GraphData() {
  private[engine] val _relations: ListBuffer[GenericRelation] = ListBuffer.empty
  private[engine] val _nodes: ListBuffer[CsvNode] = ListBuffer.empty

  /**
    * adds a node to data
    * @param ref unique Id of the node
    * @param n       instance of Node
    * @param encoder is basic serializer for value of type T.
    *              import default basic encoder from com.bharatsim.engine.basicConversions.encoders.DefaultEncoders._
    * @tparam T Type of Node
    */
  def addNode[T <: Product: ClassTag](ref: Int, n: T)(implicit encoder: BasicMapEncoder[T]): Unit = {
    _nodes.addOne(new CsvNode {
      override def label: String = fetchClassName[T]

      override def uniqueRef: NodeId = ref

      override def params: Map[String, Any] = encoder.encode(n).toMap
    })
  }

  /**
    * adds a relation to data
    * @param relations is List of Relations to be Added
    */
  def addRelations(relations: Iterable[GenericRelation]): Unit = {
    _relations.addAll(relations)
  }
}


private[engine] trait CsvNode {
  def label: String

  def uniqueRef: Int

  def params: Map[String, Any]
}

/**
 * Representation of Relation between two nodes
 * @param refFrom  unique id of "from" node
 * @param relation name of relation
 * @param refTo unique id of "to" node
 * @tparam F type of "from" node
 * @tparam T type of "to" node
 */
case class Relation[F <: Node: ClassTag, T <: Node: ClassTag](refFrom: Int, relation: String, refTo: Int) {
  private[engine] def fromLabel: String = fetchClassName[F]

  private[engine] def toLabel: String = fetchClassName[T]
}

object Relation {
  type GenericRelation = Relation[_, _]
}
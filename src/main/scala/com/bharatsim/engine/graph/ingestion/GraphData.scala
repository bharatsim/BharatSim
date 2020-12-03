package com.bharatsim.engine.graph.ingestion

import com.bharatsim.engine.basicConversions.encoders.BasicMapEncoder
import com.bharatsim.engine.models.Node
import com.bharatsim.engine.utils.Utils.fetchClassName

import scala.collection.mutable.ListBuffer
import scala.reflect.ClassTag

private[engine] case class CsvNode(label: String, uniqueRef: Int, params: Map[String, Any])

class GraphData() {
  private[engine] val _relations: ListBuffer[Relation] = ListBuffer.empty
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
    _nodes.addOne(CsvNode(fetchClassName[T], ref, encoder.encode(n).toMap))
  }

  private[engine] def addNode(csvNode: CsvNode): Unit = {
    _nodes.addOne(csvNode)
  }

  /**
    * adds a relation to data
    * @param relations is List of Relations to be Added
    */
  def addRelations(relation: Relation, relations: Relation*): Unit = {
    _relations.addOne(relation)
    _relations.addAll(relations)
  }
}

/**
  * @param fromRef unique id of "from" node
  * @param relation name of relation
  * @param toRef unique id of "to" node
  * @param fromLabel label of the from node
  * @param toLabel label of the to node
  */
case class Relation(fromLabel: String, fromRef: Int, relation: String, toLabel: String, toRef: Int)

object Relation {

  /**
    * @param fromRef unique id of "from" node
    * @param relation name of relation
    * @param toRef unique id of "to" node
    * @tparam F type of "from" node
    * @tparam T type of "to" node
    * @return Relation instance
    */
  def apply[F <: Node: ClassTag, T <: Node: ClassTag](fromRef: Int, relation: String, toRef: Int): Relation = {
    val fromLabel: String = fetchClassName[F]
    val toLabel: String = fetchClassName[T]
    Relation(fromLabel, fromRef, relation, toLabel, toRef)
  }
}

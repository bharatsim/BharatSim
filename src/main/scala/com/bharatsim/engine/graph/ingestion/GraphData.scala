package com.bharatsim.engine.graph.ingestion

import com.bharatsim.engine.basicConversions.encoders.BasicMapEncoder
import com.bharatsim.engine.models.Node
import com.bharatsim.engine.utils.Utils.fetchClassName

import scala.collection.mutable.ListBuffer
import scala.reflect.ClassTag

private[engine] case class CsvNode(label: String, uniqueRef: Long, params: Map[String, Any]) {
  override def hashCode():Int = uniqueRef.hashCode()
}

class GraphData(nodeExpander: NodeExpander) {
  private[engine] val _relations: ListBuffer[Relation] = ListBuffer.empty
  private[engine] val _nodes: ListBuffer[CsvNode] = ListBuffer.empty

  /**
   * adds a node to data
   *
   * @param ref     unique Id of the node
   * @param n       instance of Node
   * @param encoder is basic serializer for value of type T.
   *                import default basic encoder from com.bharatsim.engine.basicConversions.encoders.DefaultEncoders._
   * @tparam T Type of Node
   */
  def addNode[T <: Node : ClassTag](ref: Long, n: T)(implicit encoder: BasicMapEncoder[T]): Unit = {
    val expanded = nodeExpander.expand(ref, n)
    _nodes.addAll(expanded._nodes)
    _relations.addAll(expanded._relations)
    _nodes.addOne(CsvNode(fetchClassName[T], ref, encoder.encode(n).toMap))
  }

  private[engine] def addNode(csvNode: CsvNode): Unit = {
    _nodes.addOne(csvNode)
  }

  /**
   * adds a relation to data
   *
   * @param relations is List of Relations to be Added
   */
  def addRelations(relation: Relation, relations: Relation*): Unit = {
    _relations.addOne(relation)
    _relations.addAll(relations)
  }
}

object GraphData {
  def apply(): GraphData = new GraphData(new NodeExpander)
}

/**
 * @param fromRef   unique id of "from" node
 * @param relation  name of relation
 * @param toRef     unique id of "to" node
 * @param fromLabel label of the from node
 * @param toLabel   label of the to node
 */
case class Relation(fromLabel: String, fromRef: Long, relation: String, toLabel: String, toRef: Long)

object Relation {

  /**
    * @param fromRef unique id of "from" node
    * @param relation name of relation
    * @param toRef unique id of "to" node
    * @tparam F type of "from" node
    * @tparam T type of "to" node
    * @return Relation instance
    */
  def apply[F <: Node: ClassTag, T <: Node: ClassTag](fromRef: Long, relation: String, toRef: Long): Relation = {
    val fromLabel: String = fetchClassName[F]
    val toLabel: String = fetchClassName[T]
    Relation(fromLabel, fromRef, relation, toLabel, toRef)
  }
}

package com.bharatsim.engine.models

import com.bharatsim.engine.basicConversions.encoders.BasicEncoder
import com.bharatsim.engine.exception.MultipleRelationDefinitionsException
import com.bharatsim.engine.graph.patternMatcher.MatchPattern
import com.bharatsim.engine.graph.{GraphNode, GraphProvider, GraphProviderFactory}
import com.bharatsim.engine.models.Node.Parameter
import com.bharatsim.engine.utils.Utils

import scala.collection.mutable
import scala.reflect.ClassTag

/**
  * Node represents the unit of underling graph.
  */

class Node(private[engine] val graphProvider: GraphProvider = GraphProviderFactory.get) extends Identity {

  override var internalId: Long = 0
  private[engine] val relationSchema: mutable.HashMap[String, String] = mutable.HashMap.empty

  private[engine] def setId(newId: Long): Unit = {
    internalId = newId
  }

  /**
    *  Define Relation Between Nodes
    * @param relation is a label that describes the Relation
    * @tparam T is Type of the "TO" Node. Only one relation can be defined with same T
    * @throws com.bharatsim.engine.exception.MultipleRelationDefinitionsException
    *         when multiple relation are defined with Same `T` ("To" Node type) the exception is thrown.
    */
  @throws(classOf[MultipleRelationDefinitionsException])
  protected[engine] def addRelation[T <: Node: ClassTag](relation: String): Unit = {
    val nodeName = Utils.fetchClassName[T]
    relationSchema.get(nodeName) match {
      case Some(_) =>
        throw new MultipleRelationDefinitionsException(
          "Multiple relations are not permitted between two node" +
            " instances. Nodes " + nodeName + " and " + this.getClass.getSimpleName + " have multiple relations defined."
        )
      case None => relationSchema.addOne(nodeName, relation)
    }
  }

  /**
    * @param toNode is Class Name of "To" Node passed as T in `addRelation`
    * @return a relation label if already defined using `addRelation`
    */
  def getRelation(toNode: String): Option[String] = {
    relationSchema.get(toNode)
  }

  /**
    * @tparam T is Type of the "TO" Node
    * @return a relation label if already defined using `addRelation`
    */
  def getRelation[T <: Node: ClassTag](): Option[String] = {
    relationSchema.get(Utils.fetchClassName[T])
  }

  /**
    * Define one directional connection between two nodes
    * @param relation is a label that describes the connection
    * @param to is the other Node
    */
  def unidirectionalConnect(relation: String, to: Node): Unit = {
    graphProvider.createRelationship(internalId, relation, to.internalId)
  }

  /**
    * Define two directional connection between two nodes
    * @param relation is a label that describes the connection
    * @param to is the other Node
    */
  def bidirectionalConnect(relation: String, to: Node): Unit = {
    unidirectionalConnect(relation, to)
    to.unidirectionalConnect(relation, this)
  }

  /**
    * removes the connection between two nodes
    * @param relation is a label that describes the connection
    * @param to is the other Node
    */
  def disconnect(relation: String, to: Node): Unit = {
    graphProvider.deleteRelationship(internalId, relation, to.internalId)
  }

  /**
    * @param relation is a label that describes the connection
    * @return all the Nodes that are connected with specified relation
    */
  def getConnections(relation: String): Iterator[GraphNode] = {
    graphProvider.fetchNeighborsOf(internalId, relation).iterator
  }

  /**
    * @param relation is a label that describes the connection
    * @return count of all the nodes with specified relation
    */
  def getConnectionCount(relation: String): Int = {
    graphProvider.neighborCount(internalId, relation)
  }

  /**
    * @param relation is a label that describes the connection
    * @param matchPattern is condition to filter the nodes
    * @return count of all the matching node
    */
  def getConnectionCount(relation: String, matchPattern: MatchPattern): Int = {
    graphProvider.neighborCount(internalId, relation, matchPattern)
  }

  /**
    * Update the parameter of the node
    * @param key name of the parameter
    * @param value value for the parameter
    * @param encoder is basic serializer for value of type T.
    *   import default basic encoder from com.bharatsim.engine.basicConversions.encoders.DefaultEncoders._
    *
    * @tparam T Type of value
    */
  def updateParam[T](key: String, value: T)(implicit encoder: BasicEncoder[T]): Unit = {
    graphProvider.updateNode(internalId, (key, encoder.encode(value).get))
  }

  /**
    * Update the parameters of the node
    * @param params is List of Parameters with updated value
    */
  def updateParams(params: Parameter[_]*): Unit = {
    graphProvider.updateNode(internalId, params.map(_.get()).toMap)
  }
}

object Node {

  implicit class Parameter[T](value: (String, T))(implicit encoder: BasicEncoder[T]) {
    def get(): (String, Any) = {
      (value._1, encoder.encode(value._2).get)
    }
  }
}

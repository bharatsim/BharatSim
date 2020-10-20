package com.bharatsim.engine.models

import com.bharatsim.engine.basicConversions.encoders.BasicEncoder
import com.bharatsim.engine.exception.MultipleRelationDefinitionsException
import com.bharatsim.engine.graph.{GraphNode, GraphProvider, GraphProviderFactory}
import com.bharatsim.engine.utils.Utils

import scala.collection.mutable
import scala.reflect.ClassTag

class Node()(implicit graphProvider: GraphProvider = GraphProviderFactory.get) extends Identity {
  override var internalId: Int = 0
  private[engine] val relationSchema: mutable.HashMap[String, String] = mutable.HashMap.empty

  private[engine] def setId(newId: Int): Unit = {
    internalId = newId
  }

  @throws(classOf[MultipleRelationDefinitionsException])
  protected[engine] def addRelation[T <: Node : ClassTag](relation: String): Unit = {
    val nodeName = Utils.fetchClassName[T]
    relationSchema.get(nodeName) match {
      case Some(_) => throw new MultipleRelationDefinitionsException("Multiple relations are not permitted between two node" +
        " instances. Nodes " + nodeName + " and " + this.getClass.getSimpleName + " have multiple relations defined.")
      case None => relationSchema.addOne(nodeName, relation)
    }
  }

  def getRelation(toNode: String): Option[String] = {
    relationSchema.get(toNode)
  }

  def getRelation[T <: Node: ClassTag](): Option[String] = {
    relationSchema.get(Utils.fetchClassName[T])
  }

  def unidirectionalConnect(relation: String, to: Node): Unit = {
    graphProvider.createRelationship(internalId, relation, to.internalId)
  }

  def bidirectionalConnect(relation: String, to: Node): Unit = {
    unidirectionalConnect(relation, to)
    to.unidirectionalConnect(relation, this)
  }

  def disconnect(relation: String, to: Node): Unit = {
    graphProvider.deleteRelationship(internalId, relation, to.internalId)
  }

  def getConnections(relation: String): Iterator[GraphNode] = {
    graphProvider.fetchNeighborsOf(internalId, relation).iterator
  }

  def updateParam[T](key: String, value: T)(implicit encoder: BasicEncoder[T]): Unit = {
    graphProvider.updateNode(internalId, (key, encoder.encode(value).get))
  }
}


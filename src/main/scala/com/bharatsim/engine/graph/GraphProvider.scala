package com.bharatsim.engine.graph

import com.bharatsim.engine.basicConversions.BasicConversions
import com.bharatsim.engine.basicConversions.decoders.BasicMapDecoder
import com.bharatsim.engine.basicConversions.encoders.BasicMapEncoder
import com.bharatsim.engine.graph.GraphProvider.NodeId
import com.bharatsim.engine.graph.ingestion._
import com.bharatsim.engine.graph.patternMatcher.{EmptyPattern, MatchPattern}
import com.bharatsim.engine.models.Node
import com.bharatsim.engine.utils.Utils.fetchClassName
import com.github.tototoshi.csv.CSVReader

import scala.collection.mutable
import scala.reflect.ClassTag

/**
  * Representation of node data from the data store
  */
case class GraphNode(nodeLabel: String, id: NodeId, params: Map[String, Any] = Map.empty) {

  /**
    * label of the node
    */
  def label: String = nodeLabel

  /** Id of the node */
  def Id: NodeId = id

  /**
    * Additional parameter associated with node
    */
  def getParams: Map[String, Any] = params

  /**
    *  gets the data from the node
    * @param key parameter name
    * @return value of the parameter
    */
  def apply(key: String): Option[Any] = params.get(key)

  /**
    * Decodes the GraphNode to the node instance in the model
    * @param decoder is basic decoder for the type T
    *                import com.bharatsim.engine.basicConversions.decoders.DefaultDecoders._
    * @tparam T is type of node from model to which graph node is to be Decoded.
    * @return a decode Node as instance of T
    */
  def as[T <: Node](implicit decoder: BasicMapDecoder[T]): T = {
    val node = BasicConversions.decode(getParams)
    node.setId(Id)
    node
  }
}

case class PartialGraphNode(nodeLabel: String, id: NodeId, params: Map[String, Any])

/**
  * GraphProvider interface allows to perform CRUD operations on underlying data store
  */
trait GraphProvider {
  /* CRUD */

  /**
    * Create a node from Instance
    *
    * @param x       is instance from which a node is created
    * @param encoder is basic serializer for value of type T.
    *                import default basic encoder from com.bharatsim.engine.basicConversions.encoders.DefaultEncoders._
    * @tparam T is type of Node to be created
    * @return a node id of newly created Node.
    */
  def createNodeFromInstance[T <: Node: ClassTag](x: T)(implicit encoder: BasicMapEncoder[T]): NodeId = {
    val label = fetchClassName[T]
    val id = createNode(label, BasicConversions.encode(x))

    val nodeExpander = new NodeExpander
    val data = nodeExpander.expand[T](label, id, x)
    val refToIdMapping = batchImportNodes(data._nodes)
    refToIdMapping.addMapping(label, id, id)
    batchImportRelations(data._relations, refToIdMapping)

    id
  }

  /**
    * Create a node from Data
    *
    * @param label label of a node
    * @param props is data associated with Node.
    * @return a node id of newly created Node.
    */
  private[engine] def createNode(label: String, props: Map[String, Any]): NodeId

  /**
    * Create a node from Data
    *
    * @param label label of a node
    * @param props is data associated with Node.
    * @return a node id of newly created Node.
    */
  private[engine] def createNode(label: String, props: (String, Any)*): NodeId = createNode(label, props.toMap)

  /**
    * Crate one way relation or connection between node
    *
    * @param node1 is a "from" node
    * @param label is a name of relation
    * @param node2 is a "to" node
    */
  def createRelationship(node1: NodeId, label: String, node2: NodeId): Unit

  /**
    * ingest data from csv
    *
    * @param csvPath is path to the CSV
    * @param mapper  is function that map a csv row to Nodes and Relations
    */
  private[engine] def ingestFromCsv(csvPath: String, mapper: Option[Function[Map[String, String], GraphData]]): Unit = {
    if (mapper.isDefined) {
      val nodes = mutable.ListBuffer.empty[CsvNode]
      val relations = mutable.ListBuffer.empty[Relation]

      CSVReader.open(csvPath).toStreamWithHeaders.foreach(row => {
        val data = mapper.get.apply(row)
        nodes.addAll(data._nodes)
        relations.addAll(data._relations)
      })

      val refToIdMapping = batchImportNodes(nodes)
      batchImportRelations(relations, refToIdMapping)
    }
  }

  private[engine] def batchImportNodes(node: IterableOnce[CsvNode]): RefToIdMapping

  private[engine] def batchImportRelations(relations: IterableOnce[Relation], refToIdMapping: RefToIdMapping)

  /**
    * Fetch a node with matching label and parameters
    *
    * @param label  is label of node to find
    * @param params is data parameter of node to find
    * @return a matching node when match is found
    */
  // R
  def fetchNode(label: String, params: Map[String, Any]): Option[GraphNode]

  def fetchNode[T <: Node: ClassTag](params: Map[String, Any]): Option[GraphNode] = {
    val label = fetchClassName[T]
    fetchNode(label, params)
  }

  /**
    * Fetch all the nodes with matching label and parameters
    *
    * @param label  is label of node to find
    * @param params is data parameter of node to find
    * @return all the matching nodes
    */
  def fetchNodes(label: String, params: Map[String, Any]): Iterable[GraphNode]

  // TODO: Implement for other all stores
  def fetchNodesWithSkipAndLimit(label: String, params: Map[String, Any], skip: Int, limit: Int): Iterable[GraphNode] = Iterable.empty

  /**
    * Fetch all the nodes with matching label and parameters
    *
    * @param label is label of node to find
    * @param params is data parameter of node to find
    * @return all the matching nodes
    */
  def fetchNodes(label: String, params: (String, Any)*): Iterable[GraphNode] = fetchNodes(label, params.toMap)

  /**
    * Gets all the nodes that matches the criteria
    * @param label is label of node to find
    * @param matchPattern is matching criteria for node
    * @return all the matching nodes
    */
  def fetchNodes(label: String, matchPattern: MatchPattern): Iterable[GraphNode]

  def fetchNodesSelect(
      label: String,
      select: Set[String],
      where: MatchPattern = EmptyPattern(),
      skip: Int = 0, limit: Int = Int.MaxValue
  ): Iterable[PartialGraphNode] = Iterable.empty

  private[engine] def fetchById(id: NodeId): Option[GraphNode] = None

  /**
    * Gets count of all the nodes that matches the criteria
    * @param label is label of node to find
    * @param matchPattern is matching criteria for node
    * @return the count of all the matching nodes
    */
  def fetchCount(label: String, matchPattern: MatchPattern): Int

  /**
    *  Gets the connected node of specified nodeId with matching label
    * @param nodeId id of a node
    * @param label is label of node to find
    * @param labels additional label to find
    * @return all the connected node with matching labels.
    */
  def fetchNeighborsOf(nodeId: NodeId, label: String, labels: String*): Iterable[GraphNode]

  /**
    *  Gets the count connected node of specified nodeId
    * @param nodeId id of a node
    * @param label is label of node to find
    * @param matchCondition additional matching criteria.
    * @return count of all the matching connected node.
    */
  def neighborCount(nodeId: NodeId, label: String, matchCondition: MatchPattern = EmptyPattern()): Int

  // U
  /**
    * Update the data associated with the Node
    * @param nodeId id of the node to update
    * @param props updated data
    */
  def updateNode(nodeId: NodeId, props: Map[String, Any]): Unit

  /**
    * Update the data associated with the Node
    * @param nodeId id of the node to be updated
    * @param prop updated data
    * @param props additional updated data
    */
  def updateNode(nodeId: NodeId, prop: (String, Any), props: (String, Any)*): Unit =
    updateNode(nodeId, (prop :: props.toList).toMap)

  /**
    * delete the node.
    * @param nodeId id of node to be deleted
    */
  // D
  def deleteNode(nodeId: NodeId): Unit

  /**
    * delete the relation or connection
    * @param from id of the "from" node
    * @param label is name of relation
    * @param to id of the "to" node
    */
  def deleteRelationship(from: NodeId, label: String, to: NodeId): Unit

  /**
    * delete all the matching node
    * @param label is a label of nodes to be deleted
    * @param props is data parameter of node to be deleted
    */
  def deleteNodes(label: String, props: Map[String, Any])

  /**
    * delete all the nodes and relations
    */
  def deleteAll(): Unit

  /**
    * close the connection with data store
    */
  def shutdown(): Unit
}

object GraphProvider {
  type NodeId = Long
}

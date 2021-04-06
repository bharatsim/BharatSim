package com.bharatsim.engine.graph.neo4j

import java.util
import java.util.Date
import java.util.concurrent.ConcurrentLinkedDeque

import akka.actor.typed.ActorSystem
import com.bharatsim.engine.distributed.DBBookmark
import com.bharatsim.engine.distributed.streams.WriteOperationsStream
import com.bharatsim.engine.graph.GraphNode
import com.bharatsim.engine.graph.GraphProvider.NodeId
import com.bharatsim.engine.graph.ingestion.GraphData
import com.bharatsim.engine.graph.neo4j.queryBatching._
import com.typesafe.scalalogging.LazyLogging
import org.neo4j.driver.SessionConfig.builder
import org.neo4j.driver.Values.parameters
import org.neo4j.driver.{Bookmark, Record, Session, Transaction}

import scala.annotation.tailrec
import scala.concurrent.duration.Duration.Inf
import scala.concurrent.{Await, Future, Promise}
import scala.jdk.CollectionConverters.{IterableHasAsJava, ListHasAsScala, MapHasAsJava, MapHasAsScala}

private[engine] class BatchWriteNeo4jProvider(config: Neo4jConfig, writeParallelism: Int)
    extends Neo4jProvider(config)
    with LazyLogging {

  private var bookmarks = List.empty[Bookmark]

  def setBookmarks(bookmarks: List[DBBookmark]) = {
    this.bookmarks = bookmarks.map(b => Bookmark.from(b.values))
  }

  override def createSession: Session = {
    neo4jConnection.session(builder().withBookmarks(bookmarks.asJava).build())
  }

  private val queryQueue = new ConcurrentLinkedDeque[QueryWithPromise]();

  def fetchWithStates(label: String, skip: Int, limit: Int): Iterable[(GraphNode, Option[GraphNode])] = {
    val st = new Date().getTime
    val query = s"""match (p:$label) with p SKIP $$skip LIMIT $$limit
                   |optional match (p)-[:FSM_STATE]->(q) with p, q
                   |return {props: properties(p), labels: labels(p), id: id(p)} as node,  {props: properties(q), labels: labels(q), id: id(q)} as state""".stripMargin

    val session = neo4jConnection.session()

    val nodes = session.readTransaction((tx: Transaction) => {

      val result = tx.run(query, parameters("skip", skip, "limit", limit))

      result
        .list()
        .asScala
        .map(record => {
          val node = record.get("node").asMap()
          val state = record.get("state").asMap()
          val graphNode = extractGraphNode(node)
          if (Option(state.get("id")).isDefined)
            (graphNode, Some(extractGraphNode(state)))
          else
            (graphNode, None)
        })
    })
    session.close()
    val end = new Date().getTime

    logger.info("fetchWithStates  label {}  skip {} limit {} time {}", label, skip, limit, end - st)

    nodes.toList

  }

  private def extractGraphNode(map: java.util.Map[String, Object]) = {
    val node = map.get("props").asInstanceOf[java.util.Map[String, Object]]
    val nodeId = map.get("id").asInstanceOf[Long].toInt
    val extractedLabel = map.get("labels").asInstanceOf[util.Collection[String]]

    val mapWithValueTypeAny = node.asScala.map(kv => (kv._1, kv._2.asInstanceOf[Any])).toMap
    GraphNode(extractedLabel.iterator().next(), nodeId, mapWithValueTypeAny)
  }

  override def createRelationship(node1: NodeId, label: String, node2: NodeId): Unit = {
    val promisedRecord = Promise[Record]()
    queryQueue.push(
      QueryWithPromise(
        SubstituableQuery(
          s"""OPTIONAL MATCH (node1) WHERE id(node1) = props.nodeId1
          |OPTIONAL MATCH (node2) WHERE id(node2) = props.nodeId2
          |CREATE (node1)-[:$label]-> (node2)""".stripMargin,
          parameters("nodeId1", node1, "nodeId2", node2).asMap()
        ),
        promisedRecord
      )
    )
  }

  override def updateNode(nodeId: NodeId, props: Map[String, Any]): Unit = {
    queryQueue.push(
      QueryWithPromise(
        SubstituableQuery(
          {
            val expandedProps = toMatchCriteria("n", "props", props, ",")
            s"""OPTIONAL MATCH (n) where id(n) = props.nodeId
             |SET $expandedProps""".stripMargin
          },
          props.+(("nodeId", nodeId)).map(kv => (kv._1, kv._2.asInstanceOf[java.lang.Object])).asJava
        ),
        Promise[Record]()
      )
    )
  }

  private def toMatchCriteria(variableName: String, paramName: String, params: Map[String, Any], separator: String) = {
    params.keys.map(key => s"$variableName.$key = $paramName.$key").mkString(s" $separator ")
  }

  override def deleteNode(nodeId: NodeId): Unit = {
    queryQueue.push(
      QueryWithPromise(
        SubstituableQuery(
          s"""OPTIONAL MATCH (n) where id(n) = props.nodeId
                                     |DETACH DELETE n""".stripMargin,
          parameters("nodeId", nodeId).asMap()
        ),
        Promise[Record]()
      )
    )
  }

  override def deleteRelationship(from: NodeId, label: String, to: NodeId): Unit = {
    queryQueue.push(
      QueryWithPromise(
        SubstituableQuery(
          s"""OPTIONAL MATCH (node1) where id(node1) = props.from
             |OPTIONAL MATCH (node2) where id(node2) = props.to
             |OPTIONAL MATCH (node1)-[rel:$label]->(node2)
             |DELETE rel""".stripMargin,
          parameters("from", from, "to", to).asMap()
        ),
        Promise()
      )
    )
  }

  override def deleteNodes(label: String, props: Map[String, Any]): Unit = {
    queryQueue.push(
      QueryWithPromise(
        SubstituableQuery(
          {
            val matchCriteria = toMatchCriteria("n", "props", props, "AND")
            s"""OPTIONAL MATCH (n:$label) WHERE $matchCriteria
                DETACH DELETE n""".stripMargin
          },
          props.map(kv => (kv._1, kv._2.asInstanceOf[java.lang.Object])).asJava
        ),
        Promise()
      )
    )
  }

  def fetchNodeIds(label: String, skip: Int, limit: Int): List[NodeId] = {
    val session = neo4jConnection.session()

    val nodes = session.readTransaction((tx: Transaction) => {

      val result = tx.run(
        s"""OPTIONAL MATCH (n:$label) return id(n) as nodeId
                             |SKIP $$skip LIMIT $$limit""".stripMargin,
        parameters("skip", skip, "limit", limit)
      )

      result.list().asScala.map(record => record.get("nodeId").asInt())
    })
    session.close()
    nodes.toList
  }

  def executeWrite(query: String, params: java.util.HashMap[String, Object]): Future[Record] = {
    val p = Promise[Record]()
    queryQueue.push(QueryWithPromise(SubstituableQuery(query, params), p))
    p.future
  }

  def executePendingWrites(actorSystem: ActorSystem[_], lastBookmark: Option[Bookmark] = None): Future[Bookmark] = {
    logger.info("pending writes count {}", queryQueue.size)
    @tailrec
    def collect(
        q: ConcurrentLinkedDeque[QueryWithPromise],
        acc: List[QueryWithPromise] = List.empty
    ): List[QueryWithPromise] = {
      if (!q.isEmpty) {
        val head = q.poll()
        collect(q, head :: acc)
      } else acc.reverse
    }

    val writeOperations = collect(queryQueue)
    val newBookmark =
      Await.result(new WriteOperationsStream(neo4jConnection)(actorSystem).write(writeOperations, lastBookmark), Inf)
    if (queryQueue.isEmpty) Future(newBookmark)(actorSystem.executionContext)
    else executePendingWrites(actorSystem, Some(newBookmark))
  }

  override def ingestFromCsv(csvPath: String, mapper: Option[Function[Map[String, String], GraphData]]): Unit = {
    super.ingestFromCsv(csvPath, mapper)
  }
}

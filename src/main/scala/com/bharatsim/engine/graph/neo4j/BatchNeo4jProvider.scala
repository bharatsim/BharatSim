package com.bharatsim.engine.graph.neo4j

import java.util
import java.util.Date
import java.util.concurrent.ConcurrentLinkedDeque

import akka.actor.typed.ActorSystem
import akka.actor.typed.scaladsl.Behaviors
import com.bharatsim.engine.distributed.DBBookmark
import com.bharatsim.engine.graph.GraphNode
import com.bharatsim.engine.graph.GraphProvider.NodeId
import com.bharatsim.engine.graph.neo4j.queryBatching._
import com.bharatsim.engine.graph.patternMatcher.MatchPattern
import com.typesafe.scalalogging.LazyLogging
import org.neo4j.driver.SessionConfig.builder
import org.neo4j.driver.Values.parameters
import org.neo4j.driver.{AccessMode, Bookmark, Record, Session, Transaction}

import scala.annotation.tailrec
import scala.concurrent.duration.Duration.Inf
import scala.concurrent.{Await, Future, Promise}
import scala.jdk.CollectionConverters.{IterableHasAsJava, ListHasAsScala, MapHasAsJava, MapHasAsScala}
import scala.util.{Failure, Success}

private[engine] class BatchNeo4jProvider(config: Neo4jConfig) extends Neo4jProvider(config) with LazyLogging {

  val actorSystem: ActorSystem[_] =
    ActorSystem(Behaviors.empty[Any], "BatchNeo4jProvider")
  private var bookmarks = List.empty[Bookmark]
  val readOperations = new ReadOperationsStream(neo4jConnection)(actorSystem)
  def setBookmarks(bookmarks: List[DBBookmark]) = {
    this.bookmarks = bookmarks.map(b => Bookmark.from(b.values))
    readOperations.setBookMarks(this.bookmarks)
  }

  override def createSession(accessMode: AccessMode): Session = {
    neo4jConnection.session(builder().withDefaultAccessMode(accessMode).withBookmarks(bookmarks.asJava).build())
  }

  private val queryQueue = new ConcurrentLinkedDeque[QueryWithPromise]();
  private def makeMatchNodeQuery(label: String, params: Map[String, Any], limit: Option[Int] = None): String = {
    val limitClause = if (limit.isDefined) s"LIMIT ${limit.get}" else ""
    val matchCriteria = toMatchCriteria("n", "props", params, "and")
    val whereClause = if (params.nonEmpty) s"WHERE $matchCriteria" else ""
    s"""OPTIONAL MATCH (n:$label) $whereClause  with n, uuid
         | RETURN collect({props: properties(n), id: id(n), labels: labels(n) }) as nodes , uuid $limitClause""".stripMargin
  }

  override def fetchNode(label: String, params: Map[String, Any]): Option[GraphNode] = {
    val promisedRecord = Promise[Record]()
    val substitutableQuery =
      SubstitutableQuery(makeMatchNodeQuery(label, params, Some(1)), params.asInstanceOf[Map[String, Object]].asJava)
    readOperations.enqueue(
      QueryWithPromise(
        substitutableQuery,
        promisedRecord
      )
    )
    val record = Await.result(promisedRecord.future, Inf)
    val node = record
      .get("nodes")
      .asList()
      .asScala
      .find(v => Option(v.asInstanceOf[util.Map[String, Object]].get("id")).isDefined)
    if (node.isDefined) Some(extractGraphNode(node.get.asInstanceOf[util.Map[String, Object]])) else None
  }

  override def fetchCount(label: String, matchPattern: MatchPattern): Int = {
    val patternWithParams = PatternMaker.from(matchPattern, "n", Some("props"))
    val whereClause = if (patternWithParams.pattern.nonEmpty) s"""where ${patternWithParams.pattern}""" else ""
    val query = s"""OPTIONAL MATCH (n:$label) $whereClause with n, uuid
                   | RETURN count(n) as matchCount , uuid """.stripMargin
    val promisedRecord = Promise[Record]()
    val substitutableQuery =
      SubstitutableQuery(query, patternWithParams.params.asInstanceOf[Map[String, Object]].asJava)
    readOperations.enqueue(
      QueryWithPromise(
        substitutableQuery,
        promisedRecord
      )
    )
    val record = Await.result(promisedRecord.future, Inf)
    record.get("matchCount").asInt(0)
  }

  def fetchWithStates(label: String, skip: Int, limit: Int): Iterable[(GraphNode, Option[GraphNode])] = {
    val st = new Date().getTime
    val query = s"""match (p:$label) with p SKIP $$skip LIMIT $$limit
                   |optional match (p)-[:FSM_STATE]->(q) with p, q
                   |return {props: properties(p), labels: labels(p), id: id(p)} as node,  {props: properties(q), labels: labels(q), id: id(q)} as state""".stripMargin

    val session = createSession(AccessMode.READ)

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

    logger.debug("fetchWithStates  label {}  skip {} limit {} time {}", label, skip, limit, end - st)

    nodes.toList

  }

  override def fetchNeighborsOf(nodeId: NodeId, label: String, labels: String*): Iterator[GraphNode] = {
    val allLabels = label :: labels.toList
    val labelAOrLabelB = allLabels.mkString(" | ")

    val query = s"""OPTIONAL Match (n) where id(n) = props.nodeId with n, uuid
                   |OPTIONAL Match (n)-[:$labelAOrLabelB]->(o)
                   |RETURN collect({props: properties(o), id: id(o), labels: labels(o) }) as toNodes , uuid """.stripMargin

    val promisedRecord = Promise[Record]()
    val substituableQuery = SubstitutableQuery(query, parameters("nodeId", nodeId).asMap())
    readOperations.enqueue(
      QueryWithPromise(
        substituableQuery,
        promisedRecord
      )
    )

    val record = Await.result(promisedRecord.future, Inf)
    record
      .get("toNodes")
      .asList()
      .asScala
      .filter(v => Option(v.asInstanceOf[util.Map[String, Object]].get("id")).isDefined)
      .map(v => extractGraphNode(v.asInstanceOf[util.Map[String, Object]]))
      .iterator

  }

  override def neighborCount(nodeId: NodeId, label: String, matchCondition: MatchPattern): Int = {
    val patternWithParams = PatternMaker.from(matchCondition, "o", Some("props"))
    val paramList = "nodeId" :: nodeId :: patternWithParams.params.flatMap(kv => List(kv._1, kv._2)).toList
    val whereClause = if (patternWithParams.pattern.nonEmpty) s"""where ${patternWithParams.pattern}""" else ""
    val query = s"""OPTIONAL MATCH (n) where id(n) = props.nodeId with n, props, uuid
                   |OPTIONAL MATCH (n)-[:$label]->(o) $whereClause
                   |RETURN count(o) as matchingCount , uuid
                   |""".stripMargin

    val promisedRecord = Promise[Record]()
    val substituableQuery = SubstitutableQuery(query, parameters(paramList: _*).asMap())
    readOperations.enqueue(
      QueryWithPromise(
        substituableQuery,
        promisedRecord
      )
    )
    val record = Await.result(promisedRecord.future, Inf)

    record.get("matchingCount").asInt(0)
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
        SubstitutableQuery(
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
        SubstitutableQuery(
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
        SubstitutableQuery(
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
        SubstitutableQuery(
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
        SubstitutableQuery(
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

  override def deleteAll(): Unit = {
    val query = "OPTIONAL MATCH (n) detach delete n"
    queryQueue.push(
      QueryWithPromise(
        SubstitutableQuery(query),
        Promise()
      )
    )
  }

  def executeWrite(query: String, params: java.util.HashMap[String, Object]): Future[Record] = {
    val p = Promise[Record]()
    queryQueue.push(QueryWithPromise(SubstitutableQuery(query, params), p))
    p.future
  }

  def executePendingWrites(lastBookmark: Option[Bookmark] = None): Future[DBBookmark] = {
    logger.debug("Pending writes count {}", queryQueue.size)
    val promise = Promise[DBBookmark]()
    executeAllWritesAsync(lastBookmark, promise)
    promise.future
  }

  private def executeAllWritesAsync(lastBookmark: Option[Bookmark], promise: Promise[DBBookmark]): Unit = {
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

    val eventualWriteFinish =
      new WriteOperationsStream(neo4jConnection)(actorSystem).write(writeOperations, lastBookmark)
    eventualWriteFinish.onComplete({
      case Success(newBookmark) =>
        if (queryQueue.isEmpty) promise.success(DBBookmark(newBookmark.values()))
        else executeAllWritesAsync(Some(newBookmark), promise)
      case Failure(exception) => promise.failure(exception)
    })(actorSystem.executionContext)
  }

  private def deleteAllImmediate(deleteQuery: String): Unit = {
    val deleteCount: Int = createSession(AccessMode.WRITE).writeTransaction(tx => {
      val result = tx.run(deleteQuery)
      result.single().get("deletedCount").asInt()
    })
    if (deleteCount > 0) {
      deleteAllImmediate(deleteQuery)
    }
  }

  override def clearData(): Unit = {
    logger.debug("Cleanup started for neo4j")
    val DELETE_BATCH_SIZE = 10_000
    val queryDeleteNodes =
      s"Optional match (n) with n limit ${DELETE_BATCH_SIZE} detach delete n return count(n) as deletedCount"
    val queryDeleteRelations =
      s"Optional match ()-[r]-() with r limit ${DELETE_BATCH_SIZE} delete r return count(r) as deletedCount"
    deleteAllImmediate(queryDeleteRelations)
    deleteAllImmediate(queryDeleteNodes)
    logger.debug("Cleanup Done for neo4j")
  }

  override def shutdown() {
    readOperations.close()
    super.shutdown()
  }
}

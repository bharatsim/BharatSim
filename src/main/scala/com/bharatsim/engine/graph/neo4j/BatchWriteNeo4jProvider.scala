package com.bharatsim.engine.graph.neo4j

import java.util.concurrent.ConcurrentLinkedDeque

import akka.Done
import akka.actor.typed.ActorSystem
import com.bharatsim.engine.distributed.streams.WriteOperationsStream
import com.bharatsim.engine.graph.GraphProvider.NodeId
import com.bharatsim.engine.graph.ingestion.GraphData
import com.bharatsim.engine.graph.neo4j.queryBatching._
import com.typesafe.scalalogging.LazyLogging
import org.neo4j.driver.Values.parameters
import org.neo4j.driver.{Result, Transaction}

import scala.annotation.tailrec
import scala.concurrent.{Future, Promise}
import scala.jdk.CollectionConverters.{ListHasAsScala, MapHasAsJava}

private[engine] class BatchWriteNeo4jProvider(config: Neo4jConfig, writeParallelism: Int)
    extends Neo4jProvider(config)
    with LazyLogging {
  private val queryQueue = new ConcurrentLinkedDeque[QueryWithPromise]()

  override def createRelationship(node1: NodeId, label: String, node2: NodeId): Unit = {
    queryQueue.push(
      QueryWithPromise(
        SubstituableQuery(
          TwoParamString((node1, node2) => s"""OPTIONAL MATCH ($node1) WHERE id($node1) = props.nodeId1
                                 |OPTIONAL MATCH ($node2) WHERE id($node2) = props.nodeId2
                                 |CREATE ($node1)-[:$label]-> ($node2)""".stripMargin),
          parameters("nodeId1", node1, "nodeId2", node2).asMap()
        ),
        Promise[Result]()
      )
    )
  }

  override def updateNode(nodeId: NodeId, props: Map[String, Any]): Unit = {
    queryQueue.push(
      QueryWithPromise(
        SubstituableQuery(
          SingleParamString(n => {
            val expandedProps = toMatchCriteria(n, "props", props, ",")
            s"""OPTIONAL MATCH ($n) where id($n) = props.nodeId
                   |SET $expandedProps""".stripMargin
          }),
          props.+(("nodeId", nodeId)).map(kv => (kv._1, kv._2.asInstanceOf[java.lang.Object])).asJava
        ),
        Promise[Result]()
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
          SingleParamString(n => s"""OPTIONAL MATCH ($n) where id($n) = props.nodeId
                                     |DETACH DELETE $n""".stripMargin),
          parameters("nodeId", nodeId).asMap()
        ),
        Promise[Result]()
      )
    )
  }

  override def deleteRelationship(from: NodeId, label: String, to: NodeId): Unit = {
    queryQueue.push(
      QueryWithPromise(
        SubstituableQuery(
          TwoParamString((node1, node2) => s"""OPTIONAL MATCH ($node1) where id($node1) = props.from
                                         |OPTIONAL MATCH ($node2) where id($node2) = props.to
                                         |OPTIONAL MATCH ($node1)-[rel:$label]->($node2)
                                         |DELETE rel""".stripMargin),
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
          SingleParamString(n => {
            val matchCriteria = toMatchCriteria(n, "props", props, "AND")
            s"""OPTIONAL MATCH ($n:$label) WHERE $matchCriteria
                DETACH DELETE $n""".stripMargin
          }),
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

  def executePendingWrites(actorSystem: ActorSystem[_]): Future[Done] = {
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
    new WriteOperationsStream(neo4jConnection)(actorSystem).write(writeOperations)
  }

  override def ingestFromCsv(csvPath: String, mapper: Option[Function[Map[String, String], GraphData]]): Unit = {
    super.ingestFromCsv(csvPath, mapper)
  }
}

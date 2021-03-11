package com.bharatsim.engine.graph.neo4j

import java.util.concurrent.ConcurrentLinkedDeque

import akka.Done
import akka.actor.typed.ActorSystem
import com.bharatsim.engine.distributed.store.WriteHandler._
import com.bharatsim.engine.distributed.streams.WriteOperationsStream
import com.bharatsim.engine.graph.GraphProvider.NodeId
import com.bharatsim.engine.graph.ingestion.GraphData
import com.typesafe.scalalogging.LazyLogging
import org.neo4j.driver.Transaction
import org.neo4j.driver.Values.parameters

import scala.annotation.tailrec
import scala.concurrent.Future
import scala.jdk.CollectionConverters.CollectionHasAsScala

private[engine] class LazyWriteNeo4jProvider(config: Neo4jConfig, writeParallelism: Int)
    extends Neo4jProvider(config)
    with LazyLogging {
  private val queryQueue = new ConcurrentLinkedDeque[WriteQuery]()

  override def createRelationship(node1: NodeId, label: String, node2: NodeId): Unit = {
    queryQueue.push(CreateRelationship(node1, label, node2, replyTo = null))
  }

  override def updateNode(nodeId: NodeId, props: Map[String, Any]): Unit = {
    queryQueue.push(UpdateNode(nodeId, props, replyTo = null))
  }

  override def deleteNode(nodeId: NodeId): Unit = {
    queryQueue.push(DeleteNode(nodeId, null))
  }

  override def deleteRelationship(from: NodeId, label: String, to: NodeId): Unit = {
    queryQueue.push(DeleteRelationship(from, label, to, replyTo = null))
  }

  override def deleteNodes(label: String, props: Map[String, Any]): Unit = {
    queryQueue.push(DeleteNodes(label, props, replyTo = null))
  }

  def applyNodeIds(label: String, skip: Int, limit: Int, fn: NodeId => Unit): Int = {
    val session = neo4jConnection.session()

    val nodes = session.readTransaction((tx: Transaction) => {

      val result = tx.run(s"""MATCH (n:$label) return id(n) as nodeId
           |SKIP $$skip LIMIT $$limit""".stripMargin, parameters("skip", skip, "limit", limit))

      var countOfElements = 0
      result
        .stream()
        .map(record => record.get("nodeId").asInt())
        .forEach(nodeId => {
          countOfElements += 1
          fn(nodeId)
        })

      countOfElements
    })
    session.close()
    nodes
  }

  def fetchNodeIds(label: String, skip: Int, limit: Int): List[NodeId] = {
    val session = neo4jConnection.session()

    val nodes = session.readTransaction((tx: Transaction) => {

      val result = tx.run(
        s"""MATCH (n:$label) return id(n) as nodeId
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
    def collect(q: ConcurrentLinkedDeque[WriteQuery], acc: List[WriteQuery] = List.empty): List[WriteQuery] = {
      if (!q.isEmpty) {
        val head = q.poll()
        collect(q, head :: acc)
      } else acc
    }

    val writeOperations = collect(queryQueue)
    new WriteOperationsStream(writeParallelism)(actorSystem).write(writeOperations, executeQuery)
  }

  private def executeQuery(query: WriteQuery): Unit = {
    query match {
      case CreateRelationship(node1, label, node2, _) => super.createRelationship(node1, label, node2)
      case UpdateNode(nodeId, props, _)               => super.updateNode(nodeId, props)
      case DeleteNode(nodeId, _)                      => super.deleteNode(nodeId)
      case DeleteRelationship(from, label, to, _)     => super.deleteRelationship(from, label, to)
      case DeleteNodes(label, props, _)               => super.deleteNodes(label, props)
    }
  }

  override def ingestFromCsv(csvPath: String, mapper: Option[Function[Map[String, String], GraphData]]): Unit = {
    super.ingestFromCsv(csvPath, mapper)
  }
}
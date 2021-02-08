package com.bharatsim.engine.distributed.store

import akka.actor.typed.scaladsl.AskPattern.Askable
import akka.actor.typed.{ActorRef, ActorSystem, Scheduler}
import akka.util.Timeout
import com.bharatsim.engine.distributed.store.ActorBasedStore._
import com.bharatsim.engine.distributed.store.ReadHandler.{apply => _, _}
import com.bharatsim.engine.graph.GraphProvider.NodeId
import com.bharatsim.engine.graph.ingestion.{CsvNode, GraphData, RefToIdMapping, Relation}
import com.bharatsim.engine.graph.patternMatcher.MatchPattern
import com.bharatsim.engine.graph.{GraphNode, GraphProvider}

import scala.concurrent.Await
import scala.concurrent.duration.{Duration, DurationInt}

class ActorBasedGraphProvider(dataActorRef: ActorRef[DBQuery])(implicit val system: ActorSystem[_])
    extends GraphProvider {
  implicit val seconds: Timeout = 3.seconds
  implicit val scheduler: Scheduler = system.scheduler

  override private[engine] def createNode(label: String, props: Map[String, Any]) = ???

  override private[engine] def createNode(label: String, props: (String, Any)*) = ???

  override def createRelationship(node1: NodeId, label: String, node2: NodeId): Unit = ???

  override def ingestFromCsv(csvPath: String, mapper: Option[Function[Map[String, String], GraphData]]): Unit = ???

  override private[engine] def batchImportNodes(node: IterableOnce[CsvNode]) = ???

  override private[engine] def batchImportRelations(
      relations: IterableOnce[Relation],
      refToIdMapping: RefToIdMapping
  ): Unit = ???

  override def fetchNode(label: String, params: Map[String, Any]): Option[GraphNode] = {
    AwaitedResult(ref => FetchNode(label, params, ref)).getOptionalGraphNode
  }

  override def fetchNodes(label: String, params: Map[String, Any]): Iterable[GraphNode] = {
    AwaitedResult((ref: ActorRef[Reply]) => FetchNodes(label, params, ref)).getGraphNodes
  }

  override def fetchNodes(label: String, matchPattern: MatchPattern): Iterable[GraphNode] = {
    AwaitedResult((ref: ActorRef[Reply]) => FetchNodesByPattern(label, matchPattern, ref)).getGraphNodes
  }

  override def fetchCount(label: String, matchPattern: MatchPattern): Int = {
    AwaitedResult(ref => FetchCount(label, matchPattern, ref)).getInt
  }

  override def fetchNeighborsOf(nodeId: NodeId, label: String, labels: String*): Iterable[GraphNode] = {
    AwaitedResult(ref => FetchNeighborsOf(nodeId, label :: labels.toList, ref)).getGraphNodes
  }

  override def neighborCount(nodeId: NodeId, label: String, matchCondition: MatchPattern): Int = {
    AwaitedResult(ref => NeighborCount(nodeId, label, matchCondition, ref)).getInt
  }

  override def updateNode(nodeId: NodeId, props: Map[String, Any]): Unit = ???

  override def updateNode(nodeId: NodeId, prop: (String, Any), props: (String, Any)*): Unit = ???

  override def deleteNode(nodeId: NodeId): Unit = ???

  override def deleteRelationship(from: NodeId, label: String, to: NodeId): Unit = ???

  override def deleteNodes(label: String, props: Map[String, Any]): Unit = ???

  override def deleteAll(): Unit = ???

  override def shutdown(): Unit = ???

  private class AwaitedResult[A <: DBQuery](query: ActorRef[Reply] => A) {
    def get[B <: Reply]: B =
      Await.result(dataActorRef.ask(query), Duration.Inf) match {
        case b: B => b
      }

    def getInt: Int = get[IntReply].value
    def getGraphNodes: Iterable[GraphNode] = get[GraphNodesReply].value
    def getOptionalGraphNode: Option[GraphNode] = get[OptionalGraphNode].maybeValue
  }

  private object AwaitedResult {
    def apply[A <: DBQuery](query: ActorRef[Reply] => A): AwaitedResult[A] = new AwaitedResult(query)
  }
}
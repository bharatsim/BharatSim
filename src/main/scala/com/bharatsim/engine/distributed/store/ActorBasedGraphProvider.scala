package com.bharatsim.engine.distributed.store

import akka.actor.typed.scaladsl.AskPattern.Askable
import akka.actor.typed.{ActorRef, ActorSystem, Scheduler}
import akka.util.Timeout
import com.bharatsim.engine.ApplicationConfigFactory
import com.bharatsim.engine.distributed.store.ActorBasedStore._
import com.bharatsim.engine.distributed.store.ReadHandler.{apply => _, _}
import com.bharatsim.engine.distributed.store.WriteHandler.{apply => _, _}
import com.bharatsim.engine.graph.GraphProvider.NodeId
import com.bharatsim.engine.graph.ingestion.{CsvNode, GraphData, RefToIdMapping, Relation}
import com.bharatsim.engine.graph.patternMatcher.MatchPattern
import com.bharatsim.engine.graph.{GraphNode, GraphProvider, PartialGraphNode}

import scala.concurrent.Await
import scala.concurrent.duration.{Duration, DurationInt}

class ActorBasedGraphProvider(dataActorRef: ActorRef[DBQuery])(implicit val system: ActorSystem[_])
    extends GraphProvider {
  implicit val seconds: Timeout = 10.seconds
  implicit val scheduler: Scheduler = system.scheduler

  override def ingestFromCsv(
      csvPath: String,
      mapper: Option[Function[Map[String, String], GraphData]]
  ): Unit = {
    if (ApplicationConfigFactory.config.hasEngineMainRole()) {
      var ingested = false
      while (!ingested) {
        ingested = AwaitedResult(ref => IsIngested(csvPath, ref)).getBoolean
      }
    }
  }

  override private[engine] def createNode(label: String, props: Map[String, Any]): NodeId = {
    AwaitedResult(ref => CreateNode(label, props, ref)).getNodeId
  }

  override def createRelationship(node1: NodeId, label: String, node2: NodeId): Unit = {
    AwaitedResult(ref => CreateRelationship(node1, label, node2, ref)).await()
  }

  override def fetchNode(label: String, params: Map[String, Any]): Option[GraphNode] = {
    AwaitedResult(ref => FetchNode(label, params, ref)).getOptionalGraphNode
  }

  override def fetchNodes(label: String, params: Map[String, Any]): Iterable[GraphNode] = {
    AwaitedResult((ref: ActorRef[Reply]) => FetchNodes(label, params, ref)).getGraphNodes
  }

  override def fetchNodes(label: String, matchPattern: MatchPattern): Iterable[GraphNode] = {
    AwaitedResult((ref: ActorRef[Reply]) => FetchNodesByPattern(label, matchPattern, ref)).getGraphNodes
  }

  override def fetchNodesSelect(
      label: String,
      select: Set[String],
      where: MatchPattern,
      skip: Int,
      limit: Int
  ): Iterable[PartialGraphNode] = {
    AwaitedResult(ref => Fetch(label, select, where, skip, limit, ref)).getPartialNodes
  }

  def fetchNodeIdStream(label: String, skip: Int, limit: Int): NodeIdStreamReply = {
    AwaitedResult(ref => FetchNodeIdStream(label, skip, limit, ref)).getNodeIdStream
  }

  override def fetchById(id: NodeId): Option[GraphNode] = {
    AwaitedResult(ref => FetchByNodeId(id, ref)).getOptionalGraphNode
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

  override def updateNode(nodeId: NodeId, props: Map[String, Any]): Unit = {
    AwaitedResult(ref => UpdateNode(nodeId, props, ref)).await()
  }

  override def deleteNode(nodeId: NodeId): Unit = {
    AwaitedResult(ref => DeleteNode(nodeId, ref)).await()
  }

  override def deleteRelationship(from: NodeId, label: String, to: NodeId): Unit = {
    AwaitedResult(ref => DeleteRelationship(from, label, to, ref)).await()
  }

  override def deleteNodes(label: String, props: Map[String, Any]): Unit = {
    AwaitedResult(ref => DeleteNodes(label, props, ref)).await()
  }

  override def deleteAll(): Unit = {
    AwaitedResult(ref => DeleteAll(ref)).await()
  }

  override private[engine] def batchImportNodes(node: IterableOnce[CsvNode], refToIdMapping: RefToIdMapping) = ???

  override private[engine] def batchImportRelations(
      relations: IterableOnce[Relation],
      refToIdMapping: RefToIdMapping
  ): Unit = ???

  private[engine] def swapBuffers(): Unit = {
    AwaitedResult(ref => SwapBuffers(ref)).await()

  }
  override def shutdown(): Unit = {}

  private class AwaitedResult[A <: DBQuery](query: ActorRef[Reply] => A) {
    def get[B <: Reply]: B =
      Await.result(dataActorRef.ask(query), Duration.Inf) match {
        case b: B => b
      }

    def getInt: Int = get[IntReply].value
    def getGraphNodes: Iterable[GraphNode] = get[GraphNodesReply].value
    def getOptionalGraphNode: Option[GraphNode] = get[OptionalGraphNode].maybeValue
    def getPartialNodes: Iterable[PartialGraphNode] = get[PartialNodesReply].value
    def getNodeId: NodeId = get[NodeIdReply].value
    def getBoolean: Boolean = get[BooleanReply].value
    def await(): Unit = get[DoneReply]
    def getNodeIdStream: NodeIdStreamReply = get[NodeIdStreamReply]
  }

  private object AwaitedResult {
    def apply[A <: DBQuery](query: ActorRef[Reply] => A): AwaitedResult[A] = new AwaitedResult(query)
  }

}

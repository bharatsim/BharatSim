package com.bharatsim.engine.distributed.store

import akka.actor.typed.scaladsl.{AbstractBehavior, ActorContext, Behaviors}
import akka.actor.typed.{ActorRef, Behavior}
import akka.stream.Materializer
import akka.stream.scaladsl.{Source, StreamRefs}
import com.bharatsim.engine.distributed.store.ActorBasedStore._
import com.bharatsim.engine.distributed.store.ReadHandler._
import com.bharatsim.engine.graph.GraphNode
import com.bharatsim.engine.graph.GraphProvider.NodeId
import com.bharatsim.engine.graph.custom.GraphProviderWithBufferImpl
import com.bharatsim.engine.graph.patternMatcher.MatchPattern

import scala.collection.immutable

class ReadHandler(actorContext: ActorContext[ReadQuery], graph: GraphProviderWithBufferImpl)
    extends AbstractBehavior(actorContext) {
  override def onMessage(msg: ReadQuery): Behavior[ReadQuery] = {
    msg match {
      case FetchNode(label, params, replyTo) =>
        replyTo ! OptionalGraphNode(graph.fetchNode(label, params))
      case FetchNodes(label, params, replyTo) =>
        replyTo ! GraphNodesReply(graph.fetchNodes(label, params))
      case FetchNodesWithSkipAndLimit(label, params, skip, limit, replyTo) =>
        replyTo ! GraphNodesReply(graph.fetchNodesWithSkipAndLimit(label, params, skip, limit))
      case FetchNodesByPattern(label, pattern, replyTo) =>
        replyTo ! GraphNodesReply(graph.fetchNodes(label, pattern))
      case FetchCount(label, pattern, replyTo) => replyTo ! IntReply(graph.fetchCount(label, pattern))
      case FetchNeighborsOf(nodeId, allLabels, replyTo) =>
        replyTo ! GraphNodesReply(graph.fetchNeighborsOf(nodeId, allLabels.head, allLabels.tail: _*))
      case NeighborCount(nodeId, label, matchCondition, replyTo) =>
        replyTo ! IntReply(graph.neighborCount(nodeId, label, matchCondition))
      case Fetch(label, select, where, skip, limit, replyTo) =>
        replyTo ! PartialNodesReply(graph.fetchNodesSelect(label, select, where, skip, limit))
      case FetchByNodeId(id, replyTo) => replyTo ! OptionalGraphNode(graph.fetchById(id))
      case FetchNodeIdStream(label, skip, limit, replyTo) =>
        val values: Iterable[GraphNode] = graph.fetchNodesWithSkipAndLimit(label, Map.empty, skip, limit)
        val source = Source[NodeId](immutable.Iterable.from(values.map(_.id)))
        replyTo ! NodeIdStreamReply(source.runWith(StreamRefs.sourceRef())(Materializer.createMaterializer(context.system)), values.size)
    }
    Behaviors.same
  }
}

object ReadHandler {
  sealed trait ReadQuery extends DBQuery

  case class FetchNode(label: String, params: Map[String, Any], replyTo: ActorRef[Reply]) extends ReadQuery
  case class FetchNodes(label: String, params: Map[String, Any], replyTo: ActorRef[Reply]) extends ReadQuery
  case class FetchNodesWithSkipAndLimit(
      label: String,
      params: Map[String, Any],
      skip: Int,
      limit: Int,
      replyTo: ActorRef[Reply]
  ) extends ReadQuery
  case class FetchNodesByPattern(label: String, pattern: MatchPattern, replyTo: ActorRef[Reply]) extends ReadQuery
  case class Fetch(
      label: String,
      select: Set[String],
      where: MatchPattern,
      skip: Int,
      limit: Int,
      replyTo: ActorRef[Reply]
  ) extends ReadQuery
  case class FetchByNodeId(id: NodeId, replyTo: ActorRef[Reply]) extends ReadQuery
  case class FetchCount(label: String, matchPattern: MatchPattern, replyTo: ActorRef[Reply]) extends ReadQuery
  case class FetchNeighborsOf(nodeId: NodeId, allLabels: List[String], replyTo: ActorRef[Reply]) extends ReadQuery
  case class NeighborCount(nodeId: NodeId, label: String, matchCondition: MatchPattern, replyTo: ActorRef[Reply])
      extends ReadQuery
  case class FetchNodeIdStream(label: String, skip: Int, limit: Int, ref: ActorRef[Reply]) extends ReadQuery

  def apply(graph: GraphProviderWithBufferImpl): Behavior[ReadQuery] =
    Behaviors.setup(context => new ReadHandler(context, graph))
}

package com.bharatsim.engine.distributed.store

import akka.actor.typed.scaladsl.{AbstractBehavior, ActorContext, Behaviors}
import akka.actor.typed.{ActorRef, Behavior}
import com.bharatsim.engine.distributed.store.ActorBasedStore._
import com.bharatsim.engine.distributed.store.ReadHandler._
import com.bharatsim.engine.graph.GraphProvider.NodeId
import com.bharatsim.engine.graph.custom.{Buffer, ReadOperations}
import com.bharatsim.engine.graph.patternMatcher.MatchPattern

class ReadHandler(actorContext: ActorContext[ReadQuery], buffer: Buffer) extends AbstractBehavior(actorContext) {
  val readOperations = new ReadOperations(buffer)
  override def onMessage(msg: ReadQuery): Behavior[ReadQuery] = {
    msg match {
      case FetchNode(label, params, replyTo) =>
        replyTo ! OptionalGraphNode(readOperations.fetchNode(label, params))
      case FetchNodes(label, params, replyTo) =>
        replyTo ! GraphNodesReply(readOperations.fetchNodes(label, params))
      case FetchNodesByPattern(label, pattern, replyTo) =>
        replyTo ! GraphNodesReply(readOperations.fetchNodes(label, pattern))
      case FetchCount(label, pattern, replyTo) => replyTo ! IntReply(readOperations.fetchCount(label, pattern))
      case FetchNeighborsOf(nodeId, allLabels, replyTo) =>
        replyTo ! GraphNodesReply(readOperations.fetchNeighborsOf(nodeId, allLabels))
      case NeighborCount(nodeId, label, matchCondition, replyTo) =>
        replyTo ! IntReply(readOperations.neighborCount(nodeId, label, matchCondition))
    }
    Behaviors.same
  }
}

object ReadHandler {
  sealed trait ReadQuery extends DBQuery

  case class FetchNode(label: String, params: Map[String, Any], replyTo: ActorRef[Reply]) extends ReadQuery
  case class FetchNodes(label: String, params: Map[String, Any], replyTo: ActorRef[Reply]) extends ReadQuery
  case class FetchNodesByPattern(label: String, pattern: MatchPattern, replyTo: ActorRef[Reply]) extends ReadQuery
  case class FetchCount(label: String, matchPattern: MatchPattern, replyTo: ActorRef[Reply]) extends ReadQuery
  case class FetchNeighborsOf(nodeId: NodeId, allLabels: List[String], replyTo: ActorRef[Reply]) extends ReadQuery
  case class NeighborCount(nodeId: NodeId, label: String, matchCondition: MatchPattern, replyTo: ActorRef[Reply])
      extends ReadQuery

  def apply(buffer: Buffer): Behavior[ReadQuery] = Behaviors.setup(context => new ReadHandler(context, buffer))
}

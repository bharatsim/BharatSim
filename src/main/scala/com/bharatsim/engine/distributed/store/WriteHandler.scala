package com.bharatsim.engine.distributed.store

import akka.actor.typed.scaladsl.{AbstractBehavior, ActorContext, Behaviors}
import akka.actor.typed.{ActorRef, Behavior}
import com.bharatsim.engine.distributed.store.ActorBasedStore.{DBQuery, DoneReply, NodeIdReply, Reply}
import com.bharatsim.engine.distributed.store.WriteHandler.WriteQuery
import com.bharatsim.engine.graph.GraphProvider.NodeId
import com.bharatsim.engine.graph.custom.GraphProviderWithBufferImpl

class WriteHandler(actorContext: ActorContext[WriteQuery], graph: GraphProviderWithBufferImpl)
    extends AbstractBehavior(actorContext) {

  override def onMessage(msg: WriteQuery): Behavior[WriteQuery] = {
    msg match {
      case WriteHandler.CreateNode(label, props, replyTo) =>
        replyTo ! NodeIdReply(graph.createNode(label, props))
      case WriteHandler.CreateRelationship(node1, label, node2, replyTo) =>
        graph.createRelationship(node1, label, node2)
        replyTo ! DoneReply()
      case WriteHandler.UpdateNode(nodeId, props, replyTo) =>
        graph.updateNode(nodeId, props)
        replyTo ! DoneReply()
      case WriteHandler.DeleteNode(nodeId, replyTo) =>
        graph.deleteNode(nodeId)
        replyTo ! DoneReply()
      case WriteHandler.DeleteRelationship(from, label, to, replyTo) =>
        graph.deleteRelationship(from, label, to)
        replyTo ! DoneReply()
      case WriteHandler.DeleteNodes(label, props, replyTo) =>
        graph.deleteNodes(label, props)
        replyTo ! DoneReply()
    }
    Behaviors.same
  }
}

object WriteHandler {
  sealed trait WriteQuery extends DBQuery
  case class CreateNode(label: String, props: Map[String, Any], replyTo: ActorRef[Reply]) extends WriteQuery
  case class CreateRelationship(node1: NodeId, label: String, node2: NodeId, replyTo: ActorRef[Reply])
      extends WriteQuery
  case class UpdateNode(nodeId: NodeId, props: Map[String, Any], replyTo: ActorRef[Reply]) extends WriteQuery
  case class DeleteNode(nodeId: NodeId, replyTo: ActorRef[Reply]) extends WriteQuery
  case class DeleteRelationship(from: NodeId, label: String, to: NodeId, replyTo: ActorRef[Reply]) extends WriteQuery
  case class DeleteNodes(label: String, props: Map[String, Any], replyTo: ActorRef[Reply]) extends WriteQuery

  def apply(graph: GraphProviderWithBufferImpl): Behavior[WriteQuery] =
    Behaviors.setup(context => new WriteHandler(context, graph))
}

package com.bharatsim.engine.distributed.store

import akka.actor.typed.scaladsl.{AbstractBehavior, ActorContext, Behaviors, Routers}
import akka.actor.typed.{ActorRef, Behavior}
import com.bharatsim.engine.ApplicationConfigFactory
import com.bharatsim.engine.distributed.CborSerializable
import com.bharatsim.engine.distributed.store.ActorBasedStore.{
  BooleanReply,
  DBQuery,
  DeleteAll,
  DoneReply,
  IsIngested,
  SwapBuffers
}
import com.bharatsim.engine.distributed.store.ReadHandler.ReadQuery
import com.bharatsim.engine.distributed.store.WriteHandler.WriteQuery
import com.bharatsim.engine.graph.{GraphNode, PartialGraphNode}
import com.bharatsim.engine.graph.GraphProvider.NodeId
import com.bharatsim.engine.graph.custom.GraphProviderWithBufferImpl

private[engine] class ActorBasedStore(actorContext: ActorContext[DBQuery], graph: GraphProviderWithBufferImpl)
    extends AbstractBehavior(actorContext) {

  private def pool[T <: DBQuery](routee: Behavior[T]) = {
    Routers.pool(ApplicationConfigFactory.config.storeActorCount)(routee).withRoundRobinRouting()
  }

  private val readHandler = context.spawn(pool(ReadHandler(graph)), "read-handler")
  private val writeHandler = context.spawn(pool(WriteHandler(graph)), "write-handler")

  override def onMessage(msg: DBQuery): Behavior[DBQuery] = {
    msg match {
      case query: ReadQuery  => readHandler ! query
      case query: WriteQuery => writeHandler ! query
      case DeleteAll(replyTo) =>
        graph.deleteAll()
        replyTo ! DoneReply()
      case SwapBuffers(replyTo) =>
        graph.syncBuffers()
        replyTo ! DoneReply()
      case IsIngested(csvPath, replyTo) =>
        replyTo ! BooleanReply(graph.isIngested(csvPath))
    }
    Behaviors.same
  }
}

private[engine] object ActorBasedStore {
  trait DBQuery extends CborSerializable
  case class DeleteAll(replyTo: ActorRef[Reply]) extends DBQuery
  case class SwapBuffers(replyTo: ActorRef[Reply]) extends DBQuery
  case class IsIngested(csvPath: String, replyTo: ActorRef[Reply]) extends DBQuery

  sealed trait Reply extends CborSerializable
  case class IntReply(value: Int) extends Reply
  case class GraphNodesReply(value: Iterable[GraphNode]) extends Reply
  case class OptionalGraphNode(maybeValue: Option[GraphNode]) extends Reply
  case class PartialNodesReply(value: Iterable[PartialGraphNode]) extends Reply

  case class NodeIdReply(value: NodeId) extends Reply
  case class DoneReply() extends Reply
  case class BooleanReply(value: Boolean) extends Reply
  lazy val graphProvider: GraphProviderWithBufferImpl = GraphProviderWithBufferImpl()

  def apply(): Behavior[DBQuery] = {
    Behaviors.setup(context => new ActorBasedStore(context, graphProvider))
  }
}

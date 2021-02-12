package com.bharatsim.engine.distributed.store

import akka.actor.typed.scaladsl.{AbstractBehavior, ActorContext, Behaviors}
import akka.actor.typed.{ActorRef, Behavior}
import com.bharatsim.engine.distributed.CborSerializable
import com.bharatsim.engine.distributed.store.ActorBasedStore.{DBQuery, DeleteAll, DoneReply, SwapBuffers}
import com.bharatsim.engine.distributed.store.ReadHandler.ReadQuery
import com.bharatsim.engine.distributed.store.WriteHandler.WriteQuery
import com.bharatsim.engine.graph.GraphNode
import com.bharatsim.engine.graph.GraphProvider.NodeId
import com.bharatsim.engine.graph.custom.{Buffer, GraphProviderWithBufferImpl}

import scala.collection.concurrent.TrieMap

private[engine] class ActorBasedStore(actorContext: ActorContext[DBQuery], graph: GraphProviderWithBufferImpl)
    extends AbstractBehavior(actorContext) {
  private val readHandler = context.spawn(ReadHandler(graph), "read-handler")
  private val writeHandler = context.spawn(WriteHandler(graph), "write-handler")

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
    }
    Behaviors.same
  }
}

private[engine] object ActorBasedStore {
  trait DBQuery extends CborSerializable
  case class DeleteAll(replyTo: ActorRef[Reply]) extends DBQuery
  case class SwapBuffers(replyTo: ActorRef[Reply]) extends DBQuery

  sealed trait Reply extends CborSerializable
  case class IntReply(value: Int) extends Reply
  case class GraphNodesReply(value: Iterable[GraphNode]) extends Reply
  case class OptionalGraphNode(maybeValue: Option[GraphNode]) extends Reply

  case class NodeIdReply(value: NodeId) extends Reply
  case class DoneReply() extends Reply
  val graphProvider = GraphProviderWithBufferImpl()

  def apply(readBuffer: Buffer = Buffer(TrieMap.empty, TrieMap.empty)): Behavior[DBQuery] = {
    Behaviors.setup(context => new ActorBasedStore(context, graphProvider))
  }
}

package com.bharatsim.engine.distributed.store

import akka.actor.typed.Behavior
import akka.actor.typed.scaladsl.{AbstractBehavior, ActorContext, Behaviors}
import com.bharatsim.engine.distributed.CborSerializable
import com.bharatsim.engine.distributed.store.ActorBasedStore.{DBQuery, WriteQuery}
import com.bharatsim.engine.distributed.store.ReadHandler.ReadQuery
import com.bharatsim.engine.graph.GraphNode
import com.bharatsim.engine.graph.custom.Buffer

import scala.collection.concurrent.TrieMap

class ActorBasedStore(actorContext: ActorContext[DBQuery], readBuffer: Buffer) extends AbstractBehavior(actorContext) {
  private val readHandler = context.spawn(ReadHandler(readBuffer), "read-handler")

  override def onMessage(msg: DBQuery): Behavior[DBQuery] = {
    msg match {
      case query: ReadQuery  => readHandler ! query
      case query: WriteQuery =>
    }
    Behaviors.same
  }
}

object ActorBasedStore {
  trait DBQuery extends CborSerializable
  trait WriteQuery extends DBQuery

  sealed trait Reply extends CborSerializable
  case class IntReply(value: Int) extends Reply
  case class GraphNodesReply(value: Iterable[GraphNode]) extends Reply
  case class OptionalGraphNode(maybeValue: Option[GraphNode]) extends Reply

  def apply(readBuffer: Buffer = Buffer(TrieMap.empty, TrieMap.empty)): Behavior[DBQuery] = {
    Behaviors.setup(context => new ActorBasedStore(context, readBuffer))
  }
}

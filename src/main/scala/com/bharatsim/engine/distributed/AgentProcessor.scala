package com.bharatsim.engine.distributed

import akka.actor.typed.scaladsl.{AbstractBehavior, ActorContext, Behaviors}
import akka.actor.typed.{ActorRef, Behavior}
import com.bharatsim.engine.distributed.AgentProcessor.{Command, NotifyCompletion, UnitOfWork}
import com.bharatsim.engine.distributed.actors.DistributedTickLoop
import com.bharatsim.engine.distributed.actors.DistributedTickLoop.UnitOfWorkFinished
import com.bharatsim.engine.execution.AgentExecutor
import com.bharatsim.engine.execution.NodeWithDecoder.GenericNodeWithDecoder

class AgentProcessor(
    actorContext: ActorContext[Command],
    agentExecutor: AgentExecutor
) extends AbstractBehavior(actorContext) {
  override def onMessage(msg: Command): Behavior[Command] =
    msg match {
      case UnitOfWork(nodeWithDecoder) =>
        agentExecutor.execute(nodeWithDecoder)
        Behaviors.same
      case NotifyCompletion(parent) =>
        parent ! UnitOfWorkFinished
        Behaviors.same
    }
}

object AgentProcessor {

  sealed trait Command

  case class UnitOfWork(nodeWithDecoder: GenericNodeWithDecoder) extends Command

  case class NotifyCompletion(parent: ActorRef[DistributedTickLoop.Command]) extends Command

  def apply(agentExecutor: AgentExecutor): Behavior[Command] =
    Behaviors.setup(context => new AgentProcessor(context, agentExecutor))
}

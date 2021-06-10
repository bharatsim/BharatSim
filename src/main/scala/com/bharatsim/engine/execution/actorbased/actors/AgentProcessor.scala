package com.bharatsim.engine.execution.actorbased.actors

import akka.actor.typed.scaladsl.{AbstractBehavior, ActorContext, Behaviors}
import akka.actor.typed.{ActorRef, Behavior}
import com.bharatsim.engine.execution.AgentExecutor
import com.bharatsim.engine.execution.NodeWithDecoder.GenericNodeWithDecoder
import com.bharatsim.engine.execution.actorbased.actors.AgentProcessor.{Command, UnitOfWork}
import com.bharatsim.engine.execution.actorbased.actors.TickLoop.{UnitOfWorkFailed, UnitOfWorkFinished}

class AgentProcessor(
    actorContext: ActorContext[Command],
    agentExecutor: AgentExecutor
) extends AbstractBehavior(actorContext) {
  override def onMessage(msg: Command): Behavior[Command] =
    msg match {
      case UnitOfWork(nodeWithDecoder, parent) =>
        try {
          agentExecutor.execute(nodeWithDecoder)
          parent ! UnitOfWorkFinished
        } catch {
          case exception: Throwable => parent ! UnitOfWorkFailed(exception)
        }
        Behaviors.same
    }
}

object AgentProcessor {

  sealed trait Command

  case class UnitOfWork(nodeWithDecoder: GenericNodeWithDecoder, parent: ActorRef[TickLoop.Command]) extends Command

  def apply(agentExecutor: AgentExecutor): Behavior[Command] =
    Behaviors.setup(context => new AgentProcessor(context, agentExecutor))
}

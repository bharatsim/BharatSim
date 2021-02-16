package com.bharatsim.engine.distributed

import akka.actor.typed.scaladsl.{AbstractBehavior, ActorContext, Behaviors}
import akka.actor.typed.{ActorRef, Behavior}
import com.bharatsim.engine.Context
import com.bharatsim.engine.distributed.DistributedAgentProcessor.{Command, NotifyCompletion, UnitOfWork}
import com.bharatsim.engine.distributed.actors.DistributedTickLoop
import com.bharatsim.engine.distributed.actors.DistributedTickLoop.UnitOfWorkFinished
import com.bharatsim.engine.execution.{AgentExecutor, NodeWithDecoder}

class DistributedAgentProcessor(
    actorContext: ActorContext[Command],
    agentExecutor: AgentExecutor,
    context: Context
) extends AbstractBehavior(actorContext) {
  override def onMessage(msg: Command): Behavior[Command] =
    msg match {
      case UnitOfWork(agentId, label) =>
        val graphProvider = context.graphProvider
        val decoder = context.agentTypes.find(_.label == label).get.decoder
        val gn = graphProvider.fetchById(agentId).get
        val nodeWithDecoder = NodeWithDecoder(gn, decoder)
        agentExecutor.execute(nodeWithDecoder)
        Behaviors.same
      case NotifyCompletion(parent) =>
        parent ! UnitOfWorkFinished
        Behaviors.same
    }
}

object DistributedAgentProcessor {

  sealed trait Command extends CborSerializable

  case class UnitOfWork(agentId: Int, label: String) extends Command

  case class NotifyCompletion(parent: ActorRef[DistributedTickLoop.Command]) extends Command

  def apply(agentExecutor: AgentExecutor, simulationContext: Context): Behavior[Command] =
    Behaviors.setup(context => new DistributedAgentProcessor(context, agentExecutor, simulationContext))
}

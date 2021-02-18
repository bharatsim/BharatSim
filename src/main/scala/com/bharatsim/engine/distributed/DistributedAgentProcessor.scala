package com.bharatsim.engine.distributed

import akka.actor.typed.scaladsl.{AbstractBehavior, ActorContext, Behaviors}
import akka.actor.typed.{ActorRef, Behavior}
import com.bharatsim.engine.Context
import com.bharatsim.engine.distributed.DistributedAgentProcessor.{Command, UnitOfWork}
import com.bharatsim.engine.distributed.actors.DistributedTickLoop
import com.bharatsim.engine.distributed.actors.DistributedTickLoop.UnitOfWorkFinished
import com.bharatsim.engine.execution.{AgentExecutor, NodeWithDecoder}

class DistributedAgentProcessor(
    actorContext: ActorContext[Command],
    agentExecutor: AgentExecutor,
    simulationContext: Context
) extends AbstractBehavior(actorContext) {
  override def onMessage(msg: Command): Behavior[Command] =
    msg match {
      case UnitOfWork(agentId, label, replyTo) =>
        val graphProvider = simulationContext.graphProvider
        val decoder = simulationContext.agentTypes(label)
        val gn = graphProvider.fetchById(agentId).get
        val nodeWithDecoder = NodeWithDecoder(gn, decoder)
        agentExecutor.execute(nodeWithDecoder)
        replyTo ! UnitOfWorkFinished
        Behaviors.same
    }
}

object DistributedAgentProcessor {

  sealed trait Command extends CborSerializable

  case class UnitOfWork(agentId: Int, label: String, replyTo: ActorRef[DistributedTickLoop.Command]) extends Command

  def apply(agentExecutor: AgentExecutor, simulationContext: Context): Behavior[Command] =
    Behaviors.setup(context => new DistributedAgentProcessor(context, agentExecutor, simulationContext))
}

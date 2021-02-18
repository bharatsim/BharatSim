package com.bharatsim.engine.distributed

import akka.actor.typed.scaladsl.Behaviors
import akka.actor.typed.{ActorRef, Behavior}
import com.bharatsim.engine.Context
import com.bharatsim.engine.distributed.DistributedAgentProcessor.UnitOfWork
import com.bharatsim.engine.distributed.SimulationContextReplicator.ContextData
import com.bharatsim.engine.distributed.actors.DistributedTickLoop
import com.bharatsim.engine.distributed.actors.DistributedTickLoop.ContextUpdateDone
import com.bharatsim.engine.graph.PartialGraphNode

object WorkerManager {
  sealed trait Command extends CborSerializable

  case class WorkMessage(workload: Iterable[PartialGraphNode], replyTo: ActorRef[DistributedTickLoop.Command])
      extends Command
  case class Update(updatedContext: ContextData, replyTo: ActorRef[ContextUpdateDone]) extends Command

  def apply(router: ActorRef[DistributedAgentProcessor.Command], simulationContext: Context): Behavior[Command] =
    Behaviors.receiveMessage { msg =>
      msg match {
        case WorkMessage(workload, replyTo) =>
          workload.foreach(workUnit => router ! UnitOfWork(workUnit.id, workUnit.nodeLabel, replyTo))
        case Update(updatedContext, replyTo) =>
          simulationContext.setCurrentStep(updatedContext.currentTick)
          simulationContext.setActiveIntervention(updatedContext.activeIntervention)
          simulationContext.perTickCache.clear()
          replyTo ! ContextUpdateDone()
      }
      Behaviors.same
    }
}

package com.bharatsim.engine.distributed

import akka.actor.typed.scaladsl.Behaviors
import akka.actor.typed.{ActorRef, Behavior}
import com.bharatsim.engine.Context
import com.bharatsim.engine.distributed.DistributedAgentProcessor.UnitOfWork
import com.bharatsim.engine.distributed.SimulationContextReplicator.ContextData
import com.bharatsim.engine.distributed.actors.DistributedTickLoop.ContextUpdateDone


object WorkerManager {
  sealed trait Command extends CborSerializable

  case class WorkMessage(m: UnitOfWork) extends Command
  case class Update(updatedContext: ContextData, replyTo: ActorRef[ContextUpdateDone]) extends Command

  def apply(router: ActorRef[DistributedAgentProcessor.Command], simulationContext: Context): Behavior[Command] = Behaviors.setup(context => {
    Behaviors.receiveMessage{ msg =>
      msg match {
        case WorkMessage(m) => router ! m
        case Update(updatedContext, replyTo) =>
          simulationContext.setCurrentStep(updatedContext.currentTick)
          simulationContext.setActiveIntervention(updatedContext.activeIntervention)
          simulationContext.perTickCache.clear()
          replyTo ! ContextUpdateDone()
      }
      Behaviors.same
    }
  })
}



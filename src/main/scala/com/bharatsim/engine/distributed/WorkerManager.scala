package com.bharatsim.engine.distributed

import akka.actor.typed.scaladsl.Behaviors
import akka.actor.typed.{ActorRef, Behavior}
import com.bharatsim.engine.Context
import com.bharatsim.engine.distributed.DistributedAgentProcessor.UnitOfWork
import com.bharatsim.engine.distributed.SimulationContextReplicator.ContextData
import com.bharatsim.engine.distributed.WorkerManager._
import com.bharatsim.engine.distributed.actors.Barrier.{Die, SetWorkCount, WorkFinished}
import com.bharatsim.engine.distributed.actors.DistributedTickLoop.ContextUpdateDone
import com.bharatsim.engine.distributed.actors.WorkDistributorV2.{AckNoWork, ExhaustedFor, FetchWork}
import com.bharatsim.engine.distributed.actors.{Barrier, WorkDistributorV2}
import com.bharatsim.engine.graph.neo4j.LazyWriteNeo4jProvider

class WorkerManager(router: ActorRef[DistributedAgentProcessor.Command], simulationContext: Context) {
  def default(): Behavior[Command] =
    Behaviors.receivePartial {
      case (context, message) =>
        message match {
          case Work(label, skip, limit, sender) =>
            val adaptedToBarrierSelf: ActorRef[Barrier.Reply] =
              context.messageAdapter(barrierMessage => BarrierReply(barrierMessage))
            val barrier = context.spawn(Barrier(0, None, adaptedToBarrierSelf), "barrier")

            val idCountInConsumedStream = simulationContext.graphProvider
              .asInstanceOf[LazyWriteNeo4jProvider]
              .applyNodeIds(label, skip, limit, nodeId => router ! UnitOfWork(nodeId, label, barrier))

            if (idCountInConsumedStream > 0) {
              context.log.info("Stream had {} elements for label {} with skip {}", idCountInConsumedStream, label, skip)
              barrier ! SetWorkCount(idCountInConsumedStream)
              waitForChildren(sender)
            } else {
              barrier ! Die()
              sender ! ExhaustedFor(label)
              sender ! FetchWork(context.self)
              Behaviors.same
            }

          case NoWork(confirmTo) =>
            confirmTo ! AckNoWork(context.self)
            Behaviors.same

          case StartOfNewTick(updatedContext, replyTo) =>
            simulationContext.setCurrentStep(updatedContext.currentTick)
            simulationContext.setActiveIntervention(updatedContext.activeIntervention)
            simulationContext.perTickCache.clear()
            replyTo ! ContextUpdateDone()
            Behaviors.same

          case ExecutePendingWrites(replyTo) =>
            simulationContext.graphProvider.asInstanceOf[LazyWriteNeo4jProvider].executePendingWrites()
            replyTo ! WorkFinished()
            Behaviors.same
        }
    }

  def waitForChildren(distributor: ActorRef[WorkDistributorV2.Command]): Behavior[Command] =
    Behaviors.receivePartial {
      case (context, BarrierReply(_)) =>
        context.log.info("All children finished")
        distributor ! FetchWork(context.self)
        default()
    }
}

object WorkerManager {
  sealed trait Command extends CborSerializable
  case class StartOfNewTick(updatedContext: ContextData, replyTo: ActorRef[ContextUpdateDone]) extends Command
  case class Work(label: String, skip: Int, limit: Int, sender: ActorRef[WorkDistributorV2.Command]) extends Command
  case class NoWork(confirmTo: ActorRef[WorkDistributorV2.Command]) extends Command
  case class ChildrenFinished(distributor: ActorRef[WorkDistributorV2.Command]) extends Command
  case class ExecutePendingWrites(replyTo: ActorRef[Barrier.Request]) extends Command
  case class BarrierReply(barrierMessage: Barrier.Reply) extends Command
}

package com.bharatsim.engine.distributed

import akka.actor.typed.scaladsl.Behaviors
import akka.actor.typed.{ActorRef, Behavior}
import com.bharatsim.engine.Context
import com.bharatsim.engine.distributed.DistributedAgentProcessor.UnitOfWork
import com.bharatsim.engine.distributed.SimulationContextReplicator.ContextData
import com.bharatsim.engine.distributed.actors.DistributedTickLoop.{ContextUpdateDone, PendingWritesExecuted}
import com.bharatsim.engine.distributed.actors.WorkDistributorV2.{AckNoWork, ExhaustedFor, FetchWork}
import com.bharatsim.engine.distributed.actors.{Barrier, WorkDistributorV2}
import com.bharatsim.engine.graph.neo4j.LazyWriteNeo4jProvider
import com.typesafe.scalalogging.LazyLogging

object WorkerManager extends LazyLogging {
  sealed trait Command extends CborSerializable
  case class Update(updatedContext: ContextData, replyTo: ActorRef[ContextUpdateDone]) extends Command
  case class Work(label: String, skip: Int, limit: Int, sender: ActorRef[WorkDistributorV2.Command]) extends Command
  case class NoWork(confirmTo: ActorRef[WorkDistributorV2.Command]) extends Command
  case class ChildrenFinished(distributor: ActorRef[WorkDistributorV2.Command]) extends Command
  case class ExecutePendingWrites(replyTo: ActorRef[PendingWritesExecuted]) extends Command

  def apply(router: ActorRef[DistributedAgentProcessor.Command], simulationContext: Context): Behavior[Command] =
    Behaviors.setup(context =>
      Behaviors.receiveMessage { msg =>
        msg match {
          case Work(label, skip, limit, sender) =>
            val reply = simulationContext.graphProvider.fetchNodesWithSkipAndLimit(label, Map.empty, skip, limit)

            if (reply.nonEmpty) {
              logger.info("Stream has {} elements for label {} with skip {}", reply.size, label, skip)
              val barrier = context.spawn(Barrier(0, Some(reply.size), context.self, sender), "barrier")
              reply.foreach(nodeId => router ! UnitOfWork(nodeId.id, label, barrier))
            } else {
              sender ! ExhaustedFor(label)
              sender ! FetchWork(context.self)
            }

          case NoWork(confirmTo) => confirmTo ! AckNoWork(context.self)

          case Update(updatedContext, replyTo) =>
            simulationContext.setCurrentStep(updatedContext.currentTick)
            simulationContext.setActiveIntervention(updatedContext.activeIntervention)
            simulationContext.perTickCache.clear()
            replyTo ! ContextUpdateDone()

          case ChildrenFinished(distributor) =>
            logger.info("All children finished")
            distributor ! FetchWork(context.self)

          case ExecutePendingWrites(replyTo) =>
            simulationContext.graphProvider.asInstanceOf[LazyWriteNeo4jProvider].executePendingWrites()
            replyTo ! PendingWritesExecuted()
        }
        Behaviors.same
      }
    )
}

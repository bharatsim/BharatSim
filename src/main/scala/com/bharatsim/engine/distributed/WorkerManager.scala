package com.bharatsim.engine.distributed

import akka.actor.typed.scaladsl.Behaviors
import akka.actor.typed.{ActorRef, Behavior}
import akka.stream.Materializer
import akka.stream.scaladsl.Sink
import com.bharatsim.engine.Context
import com.bharatsim.engine.distributed.DistributedAgentProcessor.UnitOfWork
import com.bharatsim.engine.distributed.SimulationContextReplicator.ContextData
import com.bharatsim.engine.distributed.actors.DistributedTickLoop.ContextUpdateDone
import com.bharatsim.engine.distributed.actors.WorkDistributorV2.{AckNoWork, ExhaustedFor, FetchWork}
import com.bharatsim.engine.distributed.actors.{Barrier, WorkDistributorV2}
import com.bharatsim.engine.distributed.store.ActorBasedGraphProvider
import com.bharatsim.engine.graph.GraphProvider.NodeId
import com.typesafe.scalalogging.LazyLogging

import scala.concurrent.ExecutionContext

object WorkerManager extends LazyLogging {
  sealed trait Command extends CborSerializable
  case class Update(updatedContext: ContextData, replyTo: ActorRef[ContextUpdateDone]) extends Command
  case class Work(label: String, skip: Int, limit: Int, sender: ActorRef[WorkDistributorV2.Command]) extends Command
  case class NoWork(confirmTo: ActorRef[WorkDistributorV2.Command]) extends Command
  case class ChildrenFinished(distributor: ActorRef[WorkDistributorV2.Command]) extends Command

  def apply(router: ActorRef[DistributedAgentProcessor.Command], simulationContext: Context): Behavior[Command] =
    Behaviors.setup(context =>
      Behaviors.receiveMessage { msg =>
        msg match {
          case Work(label, skip, limit, sender) =>
            val reply =
              simulationContext.graphProvider
                .asInstanceOf[ActorBasedGraphProvider]
                .fetchNodeIdStream(label, skip, limit)
            val replySize = reply.size

            if (replySize > 0) {
              logger.info("Stream started for label {} with skip {}", label, skip)
              val barrier = context.spawn(Barrier(0, Some(replySize), context.self, sender), "barrier")
              reply.value.runWith(Sink.foreach[NodeId](nodeId => router ! UnitOfWork(nodeId, label, barrier)))(
                Materializer.createMaterializer(context.system)
              ).onComplete(_ => logger.info("Stream finished with skip {}", skip))(ExecutionContext.global)
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
        }
        Behaviors.same
      }
    )
}

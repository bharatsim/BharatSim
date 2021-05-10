package com.bharatsim.engine.distributed

import akka.actor.typed.scaladsl.Behaviors
import akka.actor.typed.{ActorRef, Behavior}
import ch.qos.logback.classic.LoggerContext
import com.bharatsim.engine.Context
import com.bharatsim.engine.distributed.WorkerManager._
import com.bharatsim.engine.distributed.actors.Barrier.WorkFinished
import com.bharatsim.engine.distributed.actors.DistributedTickLoop.ContextUpdateDone
import com.bharatsim.engine.distributed.actors.WorkDistributor.{AckNoWork, ExhaustedFor, FetchWork}
import com.bharatsim.engine.distributed.actors.{Barrier, WorkDistributor}
import com.bharatsim.engine.distributed.streams.AgentProcessingStream
import com.bharatsim.engine.execution.AgentExecutor
import com.bharatsim.engine.execution.control.{BehaviourControl, StateControl}
import com.bharatsim.engine.graph.neo4j.BatchNeo4jProvider
import com.typesafe.scalalogging.LazyLogging

import scala.util.{Failure, Success}

class WorkerManager(simulationContext: Context) extends LazyLogging {
  def default(): Behavior[Command] =
    Behaviors.receivePartial {
      case (context, message) =>
        message match {
          case Work(label, skip, limit, sender) =>
            context.log.info(
              "Received work for label {} with skip {} for tick {}",
              label,
              skip,
              simulationContext.getCurrentStep
            )

            val nodeIds = simulationContext.graphProvider
              .asInstanceOf[BatchNeo4jProvider]
              .fetchWithStates(label, skip, limit)

            if (nodeIds.nonEmpty) {
              context.log.info(
                "Stream has {} elements for label {} with skip {} for tick {}",
                nodeIds.size,
                label,
                skip,
                simulationContext.getCurrentStep
              )
              new AgentProcessingStream(label, simulationContext)(context.system)
                .start(nodeIds)
                .onComplete {
                  case Success(_)  => sender ! FetchWork(context.self)
                  case Failure(ex) => ex.printStackTrace()
                }(context.executionContext)
              Behaviors.same
            } else {
              sender ! ExhaustedFor(label)
              sender ! FetchWork(context.self)
              Behaviors.same
            }

          case NoWork(confirmTo) =>
            confirmTo ! AckNoWork(context.self)
            Behaviors.same

          case StartOfNewTick(updatedContext, bookmarks, replyTo) =>
            logger.info("Start Tick {}", updatedContext.currentTick)
            simulationContext.setCurrentStep(updatedContext.currentTick)
            simulationContext.setActiveIntervention(updatedContext.activeIntervention)
            simulationContext.perTickCache.clear()
            simulationContext.graphProvider
              .asInstanceOf[BatchNeo4jProvider]
              .setBookmarks(bookmarks)
            replyTo ! ContextUpdateDone()
            Behaviors.same

          case ExecutePendingWrites(replyTo) =>
            logger.info("Start Write for tick {}", simulationContext.getCurrentStep)
            simulationContext.graphProvider
              .asInstanceOf[BatchNeo4jProvider]
              .executePendingWrites()
              .onComplete {
                case Success(bookmark) =>
                  logger.info("Pending writes executed for tick {}", simulationContext.getCurrentStep)
                  replyTo ! WorkFinished(DBBookmark(bookmark.values()))
              }(context.executionContext)

            Behaviors.same
        }
    }

  def waitForChildren(distributor: ActorRef[WorkDistributor.Command]): Behavior[Command] =
    Behaviors.receivePartial {
      case (context, BarrierReply(_)) =>
        context.log.info("All children finished")
        distributor ! FetchWork(context.self)
        default()
    }
}

object WorkerManager {
  sealed trait Command extends CborSerializable
  case class StartOfNewTick(
      updatedContext: ContextData,
      bookmarks: List[DBBookmark],
      replyTo: ActorRef[ContextUpdateDone]
  ) extends Command
  case class Work(label: String, skip: Int, limit: Int, sender: ActorRef[WorkDistributor.Command]) extends Command
  case class NoWork(confirmTo: ActorRef[WorkDistributor.Command]) extends Command
  case class ChildrenFinished(distributor: ActorRef[WorkDistributor.Command]) extends Command
  case class ExecutePendingWrites(replyTo: ActorRef[Barrier.Request]) extends Command
  case class BarrierReply(barrierMessage: Barrier.Reply) extends Command
}

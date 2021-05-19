package com.bharatsim.engine.distributed

import akka.actor.typed.scaladsl.{AbstractBehavior, ActorContext, Behaviors}
import akka.actor.typed.{ActorRef, Behavior}
import com.bharatsim.engine.Context
import com.bharatsim.engine.distributed.WorkerActor._
import com.bharatsim.engine.distributed.engineMain.Barrier.WorkFinished
import com.bharatsim.engine.distributed.engineMain.DistributedTickLoop.ContextUpdateDone
import com.bharatsim.engine.distributed.engineMain.WorkDistributor.{AgentLabelExhausted, FetchWork}
import com.bharatsim.engine.distributed.engineMain.{Barrier, WorkDistributor}
import com.bharatsim.engine.distributed.streams.AgentProcessingStream
import com.bharatsim.engine.graph.neo4j.BatchNeo4jProvider
import com.typesafe.scalalogging.LazyLogging

import scala.util.{Failure, Success}

class WorkerActor(context: ActorContext[Command], simulationContext: Context)
    extends AbstractBehavior(context)
    with LazyLogging {

  override def onMessage(msg: Command): Behavior[Command] = {
    msg match {
      case Work(label, skip, limit, sender) =>
        logger.info(
          "Received work for label {} with skip {} for tick {}",
          label,
          skip,
          simulationContext.getCurrentStep
        )

        val nodesWithState = simulationContext.graphProvider
          .asInstanceOf[BatchNeo4jProvider]
          .fetchWithStates(label, skip, limit)

        if (nodesWithState.nonEmpty) {
          logger.info(
            "Stream has {} elements for label {} with skip {} for tick {}",
            nodesWithState.size,
            label,
            skip,
            simulationContext.getCurrentStep
          )
          new AgentProcessingStream(label, simulationContext)(context.system)
            .start(nodesWithState)
            .onComplete {
              case Success(_)  => sender ! FetchWork(context.self)
              case Failure(ex) => ex.printStackTrace()
            }(context.executionContext)
          Behaviors.same
        } else {
          sender ! AgentLabelExhausted(label)
          sender ! FetchWork(context.self)
          Behaviors.same
        }

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
              replyTo ! WorkFinished(Some(bookmark))
          }(context.executionContext)

        Behaviors.same
    }
  }
}

object WorkerActor {

  def apply(simulationContext: Context): Behavior[WorkerActor.Command] =
    Behaviors.setup { context =>
      new WorkerActor(context, simulationContext)
    }

  sealed trait Command extends CborSerializable
  case class StartOfNewTick(
      updatedContext: ContextData,
      bookmarks: List[DBBookmark],
      replyTo: ActorRef[ContextUpdateDone]
  ) extends Command
  case class Work(label: String, skip: Int, limit: Int, sender: ActorRef[WorkDistributor.Command]) extends Command
  case class ChildrenFinished(distributor: ActorRef[WorkDistributor.Command]) extends Command
  case class ExecutePendingWrites(replyTo: ActorRef[Barrier.Request]) extends Command
}

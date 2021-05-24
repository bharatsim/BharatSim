package com.bharatsim.engine.distributed.worker

import akka.actor.typed.receptionist.{Receptionist, ServiceKey}
import akka.actor.typed.scaladsl.{ActorContext, Behaviors}
import akka.actor.typed.{ActorRef, Behavior}
import com.bharatsim.engine.Context
import com.bharatsim.engine.distributed.engineMain.Barrier.WorkFinished
import com.bharatsim.engine.distributed.engineMain.DistributedTickLoop.StartOfNewTickAck
import com.bharatsim.engine.distributed.engineMain.WorkDistributor.{AgentLabelExhausted, FetchWork}
import com.bharatsim.engine.distributed.engineMain.{Barrier, WorkDistributor}
import com.bharatsim.engine.distributed.worker.WorkerActor._
import com.bharatsim.engine.distributed.{CborSerializable, ContextData, DBBookmark}
import com.bharatsim.engine.execution.SimulationDefinition
import com.bharatsim.engine.execution.control.{BehaviourControl, DistributeStateControl}
import com.bharatsim.engine.graph.neo4j.BatchNeo4jProvider
import com.typesafe.scalalogging.LazyLogging

import scala.util.{Failure, Success}

class WorkerActor(agentProcessor: DistributedAgentProcessor = new DistributedAgentProcessor()) extends LazyLogging {

  private def workerBehaviour(context: ActorContext[Command], simulationContext: Context): Behavior[Command] =
    Behaviors.receiveMessage {
      case Work(label, skip, limit, sender) =>
        val currentStep = simulationContext.getCurrentStep
        logger.info("Received work for label {} with skip {} for tick {}", label, skip, currentStep)

        val nodesWithState = simulationContext.graphProvider
          .asInstanceOf[BatchNeo4jProvider]
          .fetchWithStates(label, skip, limit)

        if (nodesWithState.isEmpty) {
          sender ! AgentLabelExhausted(label)
          sender ! FetchWork(context.self)
          Behaviors.same
        } else {

          logger.info(
            "Stream has {} elements for label {} with skip {} for tick {}",
            nodesWithState.size,
            label,
            skip,
            currentStep
          )
          val agentExecutor = DistributedAgentExecutor(
            new BehaviourControl(simulationContext),
            new DistributeStateControl(simulationContext)
          )
          val eventualProcessDone =
            agentProcessor.process(nodesWithState, simulationContext, agentExecutor)(context.system)

          eventualProcessDone.onComplete {
            case Success(_)  => sender ! FetchWork(context.self)
            case Failure(ex) => ex.printStackTrace()
          }(context.executionContext)

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
        replyTo ! StartOfNewTickAck()
        Behaviors.same

      case ExecutePendingWrites(replyTo) =>
        val currentStep = simulationContext.getCurrentStep
        logger.info("Start Write for tick {}", currentStep)
        simulationContext.graphProvider
          .asInstanceOf[BatchNeo4jProvider]
          .executePendingWrites()
          .onComplete {
            case Success(bookmark) =>
              logger.info("Pending writes executed for tick {}", currentStep)
              replyTo ! WorkFinished(Some(bookmark))
          }(context.executionContext)

        Behaviors.same
    }

  def start(simulationDefinition: SimulationDefinition, simulationContext: Context = Context()): Behavior[Command] =
    Behaviors.setup { context =>
      simulationDefinition.simulationBody(simulationContext)
      context.system.receptionist ! Receptionist.register(workerServiceId, context.self)
      workerBehaviour(context, simulationContext)
    }
}

object WorkerActor {
  sealed trait Command extends CborSerializable
  val workerServiceId: ServiceKey[Command] =
    ServiceKey[Command]("Worker")

  case class StartOfNewTick(
      updatedContext: ContextData,
      bookmarks: List[DBBookmark],
      replyTo: ActorRef[StartOfNewTickAck]
  ) extends Command
  case class Work(label: String, skip: Int, limit: Int, sender: ActorRef[WorkDistributor.Command]) extends Command
  case class ExecutePendingWrites(replyTo: ActorRef[Barrier.Request]) extends Command
}

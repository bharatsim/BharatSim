package com.bharatsim.engine.distributed.worker

import akka.actor.CoordinatedShutdown
import akka.actor.typed.receptionist.{Receptionist, ServiceKey}
import akka.actor.typed.scaladsl.{ActorContext, Behaviors}
import akka.actor.typed.{ActorRef, Behavior}
import com.bharatsim.engine.Context
import com.bharatsim.engine.distributed.Guardian.UserInitiatedShutdown
import com.bharatsim.engine.distributed.engineMain.Barrier.{WorkErrored, WorkFinished}
import com.bharatsim.engine.distributed.engineMain.DistributedTickLoop.StartOfNewTickAck
import com.bharatsim.engine.distributed.engineMain.WorkDistributor.{AgentLabelExhausted, FetchWork, WorkFailed}
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
        logger.debug("Received Work[label: {}, skip: {}] for tick {}", label, skip, currentStep)

        val nodesWithState = simulationContext.graphProvider
          .asInstanceOf[BatchNeo4jProvider]
          .fetchWithStates(label, skip, limit)

        if (nodesWithState.isEmpty) {
          sender ! AgentLabelExhausted(label)
          sender ! FetchWork(context.self)
          Behaviors.same
        } else {

          logger.info(
            "Got Work[size: {}, label: {}, skip: {}] for tick {}",
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
            case Success(_) => sender ! FetchWork(context.self)
            case Failure(exception) =>
              logger.error(
                "Agent Processing Failed: {} \n {}",
                exception.toString,
                exception.getStackTrace.mkString("\n")
              )
              sender ! WorkFailed(s"Agent Processing Failed ${exception.toString}", context.self)
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
        logger.debug("Started executing pending writes for tick {}", currentStep)
        simulationContext.graphProvider
          .asInstanceOf[BatchNeo4jProvider]
          .executePendingWrites()
          .onComplete {
            case Success(bookmark) =>
              logger.debug("Finished executing pending writes for tick {}", currentStep)
              replyTo ! WorkFinished(Some(bookmark))
            case Failure(exception) =>
              logger.error(
                "ExecutePendingWrites Failed: {} \n {}",
                exception.toString,
                exception.getStackTrace.mkString("\n")
              )
              replyTo ! WorkErrored(s"ExecutePendingWrites Failed ${exception.toString}", context.self)

          }(context.executionContext)

        Behaviors.same

      case Shutdown(reason, origin) =>
        logger.info("Shutdown initiated by {} : {}", origin.path, reason)
        CoordinatedShutdown(context.system).run(UserInitiatedShutdown)
        Behaviors.stopped
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
  case class Shutdown(reason: String, origin: ActorRef[_]) extends Command
}

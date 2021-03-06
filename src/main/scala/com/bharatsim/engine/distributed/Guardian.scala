package com.bharatsim.engine.distributed

import akka.actor.CoordinatedShutdown
import akka.actor.typed.receptionist.{Receptionist, ServiceKey}
import akka.actor.typed.scaladsl.AskPattern.Askable
import akka.actor.typed.scaladsl.adapter._
import akka.actor.typed.scaladsl.{ActorContext, Behaviors, Routers}
import akka.actor.typed.{ActorRef, Behavior, DispatcherSelector, Scheduler}
import akka.cluster.typed.Cluster
import akka.pattern.retry
import akka.util.Timeout
import akka.{Done, actor}
import com.bharatsim.engine.distributed.Role._
import com.bharatsim.engine.distributed.store.ActorBasedStore
import com.bharatsim.engine.distributed.store.ActorBasedStore.DBQuery
import com.bharatsim.engine.execution.AgentExecutor
import com.bharatsim.engine.execution.control.{BehaviourControl, StateControl}
import com.bharatsim.engine.graph.GraphProviderFactory
import com.bharatsim.engine.{ApplicationConfigFactory, Context, SimulationDefinition}
import com.typesafe.scalalogging.LazyLogging

import scala.concurrent.duration.{Duration, DurationInt}
import scala.concurrent.{Await, ExecutionContext, ExecutionContextExecutor, Future}
import scala.util.Success

object Guardian extends LazyLogging {
  private val storeServiceKey: ServiceKey[DBQuery] = ServiceKey[DBQuery]("DataStore")
  val workerServiceKey: ServiceKey[WorkerManager.Command] =
    ServiceKey[WorkerManager.Command]("Worker")

  private def getStoreRef(actorContext: ActorContext[Nothing]): Future[ActorRef[DBQuery]] = {
    val system = actorContext.system
    implicit val seconds: Timeout = 3.seconds
    implicit val scheduler: Scheduler = system.scheduler

    val storeList = Await.result(
      system.receptionist.ask[Receptionist.Listing](replyTo => Receptionist.find(storeServiceKey, replyTo)),
      Duration.Inf
    ) match {
      case storeServiceKey.Listing(listings) => listings
    }

    if (storeList.nonEmpty) {
      Future.successful(storeList.head)
    } else {
      Future.failed(new Exception("Data service not found"));
    }
  }

  private def awaitStoreRef(context: ActorContext[Nothing]): ActorRef[DBQuery] = {
    implicit val scheduler: actor.Scheduler = context.system.scheduler.toClassic
    implicit val executionContext: ExecutionContextExecutor = context.system.executionContext
    val retried = retry(() => getStoreRef(context), 10, 1.second)
    Await.result(retried, Duration.Inf)
  }

  private def start(context: ActorContext[Nothing], simulationDefinition: SimulationDefinition): Unit = {
    val cluster = Cluster(context.system)

    if (cluster.selfMember.hasRole(Worker.toString)) {
      GraphProviderFactory.initLazyNeo4j()
      val simulationContext = Context()
      simulationDefinition.simulationBody(simulationContext)
      createWorker(context, simulationContext)
    }

    if (cluster.selfMember.hasRole(EngineMain.toString)) {
      GraphProviderFactory.initLazyNeo4j()
      val simulationContext = Context()

      simulationDefinition.ingestionStep(simulationContext)
      logger.info("Ingestion finished")

      simulationDefinition.simulationBody(simulationContext)
      createMain(context, simulationContext)
      CoordinatedShutdown(context.system)
        .addTask(CoordinatedShutdown.PhaseBeforeServiceUnbind, "user-defined-post-actions") { () =>
          Future {
            simulationDefinition.onComplete(simulationContext)
            Done
          }(ExecutionContext.global)
        }
    }
  }

  private def createWorker(context: ActorContext[Nothing], simulationContext: Context): Unit = {
    val behaviourControl = new BehaviourControl(simulationContext)
    val stateControl = new StateControl(simulationContext)
    val agentExecutor = new AgentExecutor(behaviourControl, stateControl)

    val workerRouter: ActorRef[DistributedAgentProcessor.Command] = context.spawn(
      Routers
        .pool(ApplicationConfigFactory.config.workerActorCount)(
          DistributedAgentProcessor(agentExecutor, simulationContext)
        )
        .withRoundRobinRouting(),
      "worker-router"
    )

    val workerManager = context.spawn(new WorkerManager(workerRouter, simulationContext).default(), "worker-manager")

    context.system.receptionist ! Receptionist.register(workerServiceKey, workerManager)
  }

  private def createMain(context: ActorContext[Nothing], simulationContext: Context): Unit = {
    context.spawn(EngineMainActor(simulationContext), "EngineMain")
  }

  def apply(simulationDefinition: SimulationDefinition): Behavior[Nothing] =
    Behaviors.setup[Nothing](context => {
      start(context, simulationDefinition)
      Behaviors.empty
    })

  case object UserInitiatedShutdown extends CoordinatedShutdown.Reason
}

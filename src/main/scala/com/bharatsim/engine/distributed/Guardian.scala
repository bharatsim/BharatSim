package com.bharatsim.engine.distributed

import akka.Done
import akka.actor.CoordinatedShutdown
import akka.actor.typed.Behavior
import akka.actor.typed.receptionist.{Receptionist, ServiceKey}
import akka.actor.typed.scaladsl.{ActorContext, Behaviors}
import akka.cluster.typed.Cluster
import com.bharatsim.engine.ApplicationConfigFactory.config
import com.bharatsim.engine.distributed.Role._
import com.bharatsim.engine.graph.GraphProviderFactory
import com.bharatsim.engine.{Context, SimulationDefinition}
import com.typesafe.scalalogging.LazyLogging

import scala.concurrent.{ExecutionContext, Future}

object Guardian extends LazyLogging {
  val workerServiceKey: ServiceKey[WorkerManager.Command] =
    ServiceKey[WorkerManager.Command]("Worker")

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

      if (config.disableIngestion) {
        logger.info("ingestion skipped")
      } else {
        logger.info("Ingestion started")
        simulationDefinition.ingestionStep(simulationContext)
        logger.info("Ingestion finished")
        if (config.ingestionOnly) {
          CoordinatedShutdown(context.system).run(UserInitiatedShutdown)
          return
        }
      }

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
    val workerManager = context.spawn(new WorkerManager(simulationContext).default(), "worker-manager")

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

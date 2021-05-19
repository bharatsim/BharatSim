package com.bharatsim.engine.distributed

import akka.Done
import akka.actor.CoordinatedShutdown
import akka.actor.typed.{ActorSystem, Behavior}
import akka.actor.typed.scaladsl.AbstractBehavior
import akka.actor.typed.scaladsl.{ActorContext, Behaviors}
import com.bharatsim.engine.ApplicationConfigFactory.config
import com.bharatsim.engine.Context
import com.bharatsim.engine.distributed.Guardian.UserInitiatedShutdown
import com.bharatsim.engine.distributed.engineMain.{DistributedTickLoop, WorkerCoordinator}
import com.bharatsim.engine.execution.SimulationDefinition
import com.bharatsim.engine.execution.actions.Actions
import com.bharatsim.engine.graph.GraphProviderFactory
import com.typesafe.scalalogging.LazyLogging

import scala.concurrent.{ExecutionContext, Future}

class EngineMainActor() extends LazyLogging {

  def start(
      simulationDefinition: SimulationDefinition,
      system: ActorSystem[_],
      workerCoordinator: WorkerCoordinator = new WorkerCoordinator
  ): Behavior[DistributedTickLoop.Command] = {
    GraphProviderFactory.init()
    val simulationContext = Context()

    if (config.disableIngestion) {
      logger.info("ingestion skipped")
    } else {
      logger.info("Ingestion started")
      simulationDefinition.ingestionStep(simulationContext)
      logger.info("Ingestion finished")
      if (config.ingestionOnly) {
        CoordinatedShutdown(system).run(UserInitiatedShutdown)
        return Behaviors.stopped
      }
    }

    simulationDefinition.simulationBody(simulationContext)
    CoordinatedShutdown(system)
      .addTask(CoordinatedShutdown.PhaseBeforeServiceUnbind, "user-defined-post-actions") { () =>
        Future {
          simulationDefinition.onComplete(simulationContext)
          Done
        }(ExecutionContext.global)
      }
    DistributedTickLoop(simulationContext, new Actions(simulationContext), 1, workerCoordinator = workerCoordinator)
  }
}

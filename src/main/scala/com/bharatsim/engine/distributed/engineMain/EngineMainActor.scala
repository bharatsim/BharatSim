package com.bharatsim.engine.distributed.engineMain

import akka.Done
import akka.actor.CoordinatedShutdown
import akka.actor.typed.scaladsl.Behaviors
import akka.actor.typed.{ActorSystem, Behavior}
import com.bharatsim.engine.Context
import com.bharatsim.engine.distributed.Guardian.UserInitiatedShutdown
import com.bharatsim.engine.execution.SimulationDefinition
import com.bharatsim.engine.execution.actions.Actions
import com.typesafe.scalalogging.LazyLogging

import scala.concurrent.{ExecutionContext, Future}

class EngineMainActor() extends LazyLogging {

  def start(
      simulationDefinition: SimulationDefinition,
      system: ActorSystem[_],
      simulationContext: Context = Context(),
      workerCoordinator: WorkerCoordinator = new WorkerCoordinator
  ): Behavior[DistributedTickLoop.Command] = {

    val config = simulationContext.simulationConfig
    if (config.disableIngestion) {
      logger.info("Ingestion skipped")
    } else {
      simulationDefinition.ingestionStep(simulationContext)
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

package com.bharatsim.engine.distributed

import akka.actor.CoordinatedShutdown
import akka.actor.typed.Behavior
import akka.actor.typed.scaladsl.{ActorContext, Behaviors}
import akka.cluster.typed.Cluster
import com.bharatsim.engine.distributed.Role._
import com.bharatsim.engine.execution.SimulationDefinition
import com.typesafe.scalalogging.LazyLogging

object Guardian extends LazyLogging {

  val WORKER_MANAGER = "worker-manager"
  val ENGINE_MAIN = "engine-main"

  private def start(context: ActorContext[Nothing], simulationDefinition: SimulationDefinition): Unit = {
    val cluster = Cluster(context.system)
    if (cluster.selfMember.hasRole(Worker.toString)) {
      val workerManager = new WorkerManager()
      context.spawn(workerManager.start(simulationDefinition), WORKER_MANAGER)
    }
    if (cluster.selfMember.hasRole(EngineMain.toString)) {
      val engineMain = new EngineMainActor()
      context.spawn(engineMain.start(simulationDefinition, context.system), ENGINE_MAIN)
    }
  }

  def apply(simulationDefinition: SimulationDefinition): Behavior[Nothing] =
    Behaviors.setup[Nothing](context => {
      start(context, simulationDefinition)
      Behaviors.empty
    })

  case object UserInitiatedShutdown extends CoordinatedShutdown.Reason
}

package com.bharatsim.engine.distributed

import akka.actor.CoordinatedShutdown
import akka.actor.typed.Behavior
import akka.actor.typed.scaladsl.Behaviors
import akka.cluster.typed.Cluster
import com.bharatsim.engine.distributed.Role._
import com.bharatsim.engine.distributed.engineMain.EngineMainActor
import com.bharatsim.engine.distributed.worker.WorkerActor
import com.bharatsim.engine.execution.SimulationDefinition
import com.typesafe.scalalogging.LazyLogging

class Guardian(engineMain: EngineMainActor = new EngineMainActor(), worker: WorkerActor = new WorkerActor())
    extends LazyLogging {

  val WORKER_MANAGER = "worker-manager"
  val ENGINE_MAIN = "engine-main"

  def start(simulationDefinition: SimulationDefinition): Behavior[Nothing] =
    Behaviors.setup[Nothing](context => {
      val cluster = Cluster(context.system)
      if (cluster.selfMember.hasRole(Worker.toString)) {
        context.spawn(worker.start(simulationDefinition), WORKER_MANAGER)
      }
      if (cluster.selfMember.hasRole(EngineMain.toString)) {
        context.spawn(engineMain.start(simulationDefinition, context.system), ENGINE_MAIN)
      }
      Behaviors.empty
    })
}
object Guardian {
  case object UserInitiatedShutdown extends CoordinatedShutdown.Reason
}

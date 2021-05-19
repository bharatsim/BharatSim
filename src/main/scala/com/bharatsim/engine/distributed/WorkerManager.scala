package com.bharatsim.engine.distributed

import akka.actor.typed.Behavior
import akka.actor.typed.receptionist.{Receptionist, ServiceKey}
import akka.actor.typed.scaladsl.Behaviors
import com.bharatsim.engine.Context
import com.bharatsim.engine.distributed.WorkerManager.workerServiceId
import com.bharatsim.engine.execution.SimulationDefinition
import com.bharatsim.engine.graph.GraphProviderFactory

class WorkerManager() {

  def start(simulationDefinition: SimulationDefinition): Behavior[WorkerActor.Command] =
    Behaviors.setup { context =>
      GraphProviderFactory.init()
      val simulationContext = Context()
      simulationDefinition.simulationBody(simulationContext)
      context.system.receptionist ! Receptionist.register(workerServiceId, context.self)
      WorkerActor(simulationContext)
    }
}

object WorkerManager {
  val workerServiceId: ServiceKey[WorkerActor.Command] =
    ServiceKey[WorkerActor.Command]("Worker")
}

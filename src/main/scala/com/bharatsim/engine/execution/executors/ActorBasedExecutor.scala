package com.bharatsim.engine.execution.executors

import com.bharatsim.engine.{ApplicationConfigFactory, Context}
import com.bharatsim.engine.execution.Simulation.applicationConfig
import com.bharatsim.engine.execution.actorbased.ActorBackedSimulation
import com.bharatsim.engine.execution.{SimulationDefinition, SimulationExecutor}

import scala.concurrent.Await
import scala.concurrent.duration.Duration

class ActorBasedExecutor extends DefaultExecutor {

  override def runSimulation(context: Context): Unit = {
    val actorBackedSimulation = new ActorBackedSimulation(ApplicationConfigFactory.config)
    val eventualDone = actorBackedSimulation.run(context)
    Await.ready(eventualDone, Duration.Inf)
  }

}

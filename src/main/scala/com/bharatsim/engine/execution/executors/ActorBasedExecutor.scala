package com.bharatsim.engine.execution.executors

import com.bharatsim.engine.Context
import com.bharatsim.engine.execution.actorbased.ActorBackedSimulation

import scala.concurrent.Await
import scala.concurrent.duration.Duration

class ActorBasedExecutor(actorBackedSimulation: ActorBackedSimulation = new ActorBackedSimulation())
    extends DefaultExecutor {
  override def runSimulation(context: Context): Unit = {
    val eventualDone = actorBackedSimulation.run(context)
    Await.ready(eventualDone, Duration.Inf)
  }

}

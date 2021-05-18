package com.bharatsim.engine.distributed

import akka.actor.typed.Behavior
import com.bharatsim.engine.Context
import com.bharatsim.engine.distributed.actors.DistributedTickLoop
import com.bharatsim.engine.execution.actions.Actions
import com.typesafe.scalalogging.LazyLogging

object EngineMainActor extends LazyLogging {
  def apply(simulationContext: Context): Behavior[DistributedTickLoop.Command] = {
    val actions = new Actions(simulationContext)
    actions.preSimulation.execute()
    DistributedTickLoop(simulationContext, actions, 1)
  }
}

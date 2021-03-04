package com.bharatsim.engine.distributed

import akka.actor.typed.{ActorRef, Behavior}
import com.bharatsim.engine.Context
import com.bharatsim.engine.distributed.actors.DistributedTickLoop
import com.bharatsim.engine.distributed.store.ActorBasedStore.DBQuery
import com.bharatsim.engine.execution.simulation.{PostSimulationActions, PreSimulationActions}
import com.bharatsim.engine.execution.tick.{PostTickActions, PreTickActions}
import com.typesafe.scalalogging.LazyLogging

object EngineMainActor extends LazyLogging {

  def executeRun(simulationContext: Context): Behavior[DistributedTickLoop.Command] = {
    val preSimulationActions = new PreSimulationActions(simulationContext)
    val postSimulationActions = new PostSimulationActions(simulationContext)
    val preTickActions = new PreTickActions(simulationContext)
    val postTickActions = new PostTickActions(simulationContext)
    preSimulationActions.execute()
    val tickLoop = new DistributedTickLoop(simulationContext, preTickActions, postTickActions, postSimulationActions)

    tickLoop.Tick(1)
  }

  def apply(simulationContext: Context): Behavior[DistributedTickLoop.Command] = executeRun(simulationContext)
}

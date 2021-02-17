package com.bharatsim.engine.distributed

import akka.actor.typed.scaladsl.{ActorContext, Behaviors}
import akka.actor.typed.{ActorRef, Behavior}
import com.bharatsim.engine.Context
import com.bharatsim.engine.distributed.actors.DistributedTickLoop
import com.bharatsim.engine.distributed.store.ActorBasedStore.DBQuery
import com.bharatsim.engine.execution.simulation.{PostSimulationActions, PreSimulationActions}
import com.bharatsim.engine.execution.tick.{PostTickActions, PreTickActions}
import com.typesafe.scalalogging.LazyLogging

import scala.concurrent.ExecutionContext
import scala.util.Success

object EngineMainActor extends LazyLogging {

  def executeRun(
      actorContext: ActorContext[DistributedTickLoop.Command],
      simulationContext: Context
  ): Behavior[DistributedTickLoop.Command] = {
    val preSimulationActions = new PreSimulationActions(simulationContext)
    val postSimulationActions = new PostSimulationActions(simulationContext)
    val preTickActions = new PreTickActions(simulationContext)
    val postTickActions = new PostTickActions(simulationContext)
    preSimulationActions.execute()
    val contextReplicator =
      actorContext.spawn(SimulationContextReplicator(simulationContext), "simulationContextReplicator")
    val tickLoop = new DistributedTickLoop(
      simulationContext,
      preTickActions,
      postTickActions,
      postSimulationActions,
      contextReplicator
    )

    tickLoop.Tick(1)
  }

  def apply(storeRef: ActorRef[DBQuery], simulationContext: Context): Behavior[DistributedTickLoop.Command] =
    Behaviors.setup[DistributedTickLoop.Command](context => {
      executeRun(context, simulationContext)
    })

}

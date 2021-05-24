package com.bharatsim.engine.execution.actorbased

import akka.Done
import akka.actor.typed.ActorSystem
import com.bharatsim.engine.Context
import com.bharatsim.engine.execution.actorbased.actors.TickLoop
import com.bharatsim.engine.execution.executors.ExecutorContext
import com.typesafe.scalalogging.LazyLogging

import scala.concurrent.{ExecutionContext, Future}
import scala.util.{Failure, Success}

class ActorBackedSimulation() extends LazyLogging {
  def run(context: Context): Future[Done] = {
    val (agentExecutor, actions) = new ExecutorContext().prepare(context)
    actions.preSimulation.execute()
    val tickLoop = new TickLoop(
      context,
      context.simulationConfig,
      actions.preTick,
      agentExecutor,
      actions.postTick
    )
    val actorSystem = ActorSystem(tickLoop.Tick(1), "ticks-loop")
    val executionContext = ExecutionContext.global
    actorSystem.whenTerminated.andThen {
      case Failure(exception) =>
        logger.error("Error occurred while executing simulation using actor system: {}", exception.getMessage)
        actions.postSimulation.execute()
      case Success(_) =>
        logger.info("Finished running simulation")
        actions.postSimulation.execute()
    }(executionContext)
  }
}

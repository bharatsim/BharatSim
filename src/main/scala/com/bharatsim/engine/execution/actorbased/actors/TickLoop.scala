package com.bharatsim.engine.execution.actorbased.actors

import akka.actor.typed.scaladsl.{AbstractBehavior, ActorContext, Behaviors}
import akka.actor.typed.{ActorRef, Behavior}
import com.bharatsim.engine.execution.AgentExecutor
import com.bharatsim.engine.execution.NodeWithDecoder.GenericNodeWithDecoder
import com.bharatsim.engine.execution.actorbased.RoundRobinStrategy
import com.bharatsim.engine.execution.actorbased.actors.AgentProcessor.{NotifyCompletion, UnitOfWork}
import com.bharatsim.engine.execution.actorbased.actors.TickLoop.{Command, CurrentTick, UnitOfWorkFinished}
import com.bharatsim.engine.execution.tick.{PostTickActions, PreTickActions}
import com.bharatsim.engine.{ApplicationConfig, Context}

class TickLoop(
                simulationContext: Context,
                applicationConfig: ApplicationConfig,
                preTickActions: PreTickActions,
                agentExecutor: AgentExecutor,
                postTickActions: PostTickActions
              ) {

  class Tick(actorContext: ActorContext[Command], currentTick: Int) extends AbstractBehavior(actorContext) {

    actorContext.self ! CurrentTick

    override def onMessage(msg: Command): Behavior[Command] =
      msg match {
        case CurrentTick =>
          val endOfSimulation =
            currentTick > simulationContext.simulationConfig.simulationSteps || simulationContext.stopSimulation
          if (endOfSimulation) {
            Behaviors.stopped
          } else {
            preTickActions.execute(currentTick)

            val roundRobinStrategy = new RoundRobinStrategy(applicationConfig.numProcessingActors)
            val nodesWithDecoders = simulationContext.registeredNodesWithDecoder

            nodesWithDecoders.foreach((nodeWithDecoder: GenericNodeWithDecoder) => {
              val actorName = s"processing-actor-${roundRobinStrategy.next}"
              val processingActor = getOrCreateChildActor(actorName)

              processingActor ! UnitOfWork(nodeWithDecoder)
              processingActor ! NotifyCompletion(actorContext.self)
            })

            TickBarrier(currentTick, nodesWithDecoders.size, 0)
          }
      }

    private def getOrCreateChildActor(actorName: String): ActorRef[AgentProcessor.Command] = {
      actorContext.child(actorName) match {
        case Some(value: ActorRef[AgentProcessor.Command]) => value
        case None => actorContext.spawn(AgentProcessor(agentExecutor), actorName)
      }
    }
  }

  object Tick {
    def apply(currentTick: Int): Behavior[Command] = {
      Behaviors.setup(context => new Tick(context, currentTick))
    }
  }

  class TickBarrier(actorContext: ActorContext[Command], currentTick: Int, totalUnits: Int, finishedUnits: Int)
    extends AbstractBehavior(actorContext) {
    override def onMessage(msg: TickLoop.Command): Behavior[Command] =
      msg match {
        case UnitOfWorkFinished =>
          //          println(finishedUnits + 1, totalUnits)
          if (finishedUnits + 1 == totalUnits) {
            postTickActions.execute()
            Tick(currentTick + 1)
          } else {
            TickBarrier(currentTick, totalUnits, finishedUnits + 1)
          }
      }
  }

  private object TickBarrier {
    def apply(currentTick: Int, totalUnits: Int, finishedUnits: Int): Behavior[TickLoop.Command] = {
      Behaviors.setup(context => new TickBarrier(context, currentTick, totalUnits, finishedUnits))
    }
  }

}

object TickLoop {

  sealed trait Command

  case object CurrentTick extends Command

  case object UnitOfWorkFinished extends Command

}

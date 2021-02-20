package com.bharatsim.engine.distributed.actors

import akka.actor.CoordinatedShutdown
import akka.actor.typed.receptionist.Receptionist
import akka.actor.typed.scaladsl.AskPattern.Askable
import akka.actor.typed.scaladsl.{AbstractBehavior, ActorContext, Behaviors}
import akka.actor.typed.{ActorRef, Behavior, Scheduler}
import akka.util.Timeout
import com.bharatsim.engine.Context
import com.bharatsim.engine.distributed.Guardian.{UserInitiatedShutdown, workerServiceKey}
import com.bharatsim.engine.distributed.SimulationContextReplicator.ContextData
import com.bharatsim.engine.distributed.WorkerManager.Update
import com.bharatsim.engine.distributed.actors.DistributedTickLoop.{
  AllWorkFinished,
  Command,
  ContextUpdateDone,
  CurrentTick
}
import com.bharatsim.engine.distributed.actors.WorkDistributor.FetchForNextLabel
import com.bharatsim.engine.distributed.store.ActorBasedGraphProvider
import com.bharatsim.engine.distributed.{CborSerializable, WorkerManager}
import com.bharatsim.engine.execution.simulation.PostSimulationActions
import com.bharatsim.engine.execution.tick.{PostTickActions, PreTickActions}

import scala.concurrent.duration.Duration.Inf
import scala.concurrent.duration.{Duration, DurationInt}
import scala.concurrent.{Await, ExecutionContext, Future}

class DistributedTickLoop(
    simulationContext: Context,
    preTickActions: PreTickActions,
    postTickActions: PostTickActions,
    postSimulationActions: PostSimulationActions
) {

  class Tick(actorContext: ActorContext[Command], currentTick: Int) extends AbstractBehavior(actorContext) {

    actorContext.self ! CurrentTick

    override def onMessage(msg: Command): Behavior[Command] =
      msg match {
        case CurrentTick =>
          val endOfSimulation =
            currentTick > simulationContext.simulationConfig.simulationSteps || simulationContext.stopSimulation
          if (endOfSimulation) {
            postSimulationActions.execute()
            CoordinatedShutdown(context.system).run(UserInitiatedShutdown)
            Behaviors.stopped
          } else {
            simulationContext.graphProvider.asInstanceOf[ActorBasedGraphProvider].swapBuffers()

            preTickActions.execute(currentTick)

            implicit val seconds: Timeout = 3.seconds
            implicit val scheduler: Scheduler = context.system.scheduler

            val workerList: Array[ActorRef[WorkerManager.Command]] = Await.result(
              context.system.receptionist.ask[Receptionist.Listing](replyTo =>
                Receptionist.find(workerServiceKey, replyTo)
              ),
              Duration.Inf
            ) match {
              case workerServiceKey.Listing(listings) => listings.toArray
            }

            Await.result(
              Future.foldLeft(
                workerList.map(worker =>
                  worker.ask((replyTo: ActorRef[ContextUpdateDone]) => {
                    val updatedContext =
                      ContextData(simulationContext.getCurrentStep, simulationContext.activeInterventionNames)
                    Update(updatedContext, replyTo)
                  })
                )
              )()((_, _) => ())(ExecutionContext.global),
              Inf
            )

            val barrier = actorContext.spawn(Barrier(0, None, actorContext.self), "barrier")
            val distributor = actorContext.spawn(
              WorkDistributor(workerList, barrier, simulationContext),
              "distributor"
            )

            distributor ! FetchForNextLabel

            Behaviors.same
          }

        case AllWorkFinished =>
          postTickActions.execute()
          Tick(currentTick + 1)
      }

  }

  object Tick {
    def apply(currentTick: Int): Behavior[Command] = {
      Behaviors.setup(context => new Tick(context, currentTick))
    }
  }
}

object DistributedTickLoop {

  sealed trait Command extends CborSerializable

  case object CurrentTick extends Command
  case object AllWorkFinished extends Command

  case class ContextUpdateDone() extends CborSerializable

}

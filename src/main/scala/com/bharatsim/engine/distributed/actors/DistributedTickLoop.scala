package com.bharatsim.engine.distributed.actors

import akka.actor.CoordinatedShutdown
import akka.actor.typed.receptionist.Receptionist
import akka.actor.typed.scaladsl.AskPattern.Askable
import akka.actor.typed.scaladsl.{AbstractBehavior, ActorContext, Behaviors}
import akka.actor.typed.{ActorRef, Behavior, Scheduler}
import akka.util.Timeout
import com.bharatsim.engine.Context
import com.bharatsim.engine.distributed.DistributedAgentProcessor.UnitOfWork
import com.bharatsim.engine.distributed.Guardian.{UserInitiatedShutdown, workerServiceKey}
import com.bharatsim.engine.distributed.SimulationContextReplicator.ContextData
import com.bharatsim.engine.distributed.WorkerManager.{Update, WorkMessage}
import com.bharatsim.engine.distributed.actors.DistributedTickLoop.{Command, ContextUpdateDone, CurrentTick, UnitOfWorkFinished}
import com.bharatsim.engine.distributed.store.ActorBasedGraphProvider
import com.bharatsim.engine.distributed.{CborSerializable, WorkerManager}
import com.bharatsim.engine.execution.actorbased.RoundRobinStrategy
import com.bharatsim.engine.execution.simulation.PostSimulationActions
import com.bharatsim.engine.execution.tick.{PostTickActions, PreTickActions}
import com.bharatsim.engine.graph.patternMatcher.EmptyPattern

import scala.annotation.tailrec
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

    private def sendWorkLoad(workerList: Array[ActorRef[WorkerManager.Command]]): Int = {
      val roundRobinStrategy = new RoundRobinStrategy(workerList.length)

      @tailrec
      def fetchForLabel(label: String, skip: Int, limit: Int, total: Int): Int = {
        val graphNodes = simulationContext.graphProvider.fetchNodesSelect(label, Set.empty, EmptyPattern(), skip, limit)
        val worker = workerList(roundRobinStrategy.next)
        worker ! WorkMessage(graphNodes, actorContext.self)

        if (graphNodes.isEmpty) total
        else {
          val fetchedInThisCycle = graphNodes.size
          fetchForLabel(label, skip + fetchedInThisCycle, limit, total + fetchedInThisCycle)
        }
      }

      @tailrec
      def fetchForAllLabels(labels: Iterator[String], count: Int): Int = {
        if (labels.hasNext) {
          val label = labels.next()
          val sentCount = fetchForLabel(label, 0, 500, 0)
          fetchForAllLabels(labels, count + sentCount)
        } else {
          count
        }
      }

      fetchForAllLabels(simulationContext.agentLabels.iterator, 0)
    }

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

            val workerList = Await.result(
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

            val totalWorkload = sendWorkLoad(workerList)

            TickBarrier(currentTick, totalWorkload, 0)
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
    override def onMessage(msg: DistributedTickLoop.Command): Behavior[Command] =
      msg match {
        case UnitOfWorkFinished =>
          if (finishedUnits + 1 == totalUnits) {
            postTickActions.execute()
            Tick(currentTick + 1)
          } else {
            TickBarrier(currentTick, totalUnits, finishedUnits + 1)
          }
      }
  }

  private object TickBarrier {
    def apply(currentTick: Int, totalUnits: Int, finishedUnits: Int): Behavior[DistributedTickLoop.Command] = {
      Behaviors.setup(context => new TickBarrier(context, currentTick, totalUnits, finishedUnits))
    }
  }

}

object DistributedTickLoop {

  sealed trait Command extends CborSerializable

  case object CurrentTick extends Command

  case object UnitOfWorkFinished extends Command

  case class ContextUpdateDone() extends CborSerializable
}

package com.bharatsim.engine.distributed.actors

import akka.actor.CoordinatedShutdown
import akka.actor.typed.receptionist.Receptionist
import akka.actor.typed.scaladsl.AskPattern.Askable
import akka.actor.typed.scaladsl.{AbstractBehavior, ActorContext, Behaviors}
import akka.actor.typed.{ActorRef, Behavior, Scheduler}
import akka.util.Timeout
import com.bharatsim.engine.Context
import com.bharatsim.engine.distributed.{CborSerializable, DBBookmark}
import com.bharatsim.engine.distributed.Guardian.{UserInitiatedShutdown, workerServiceKey}
import com.bharatsim.engine.distributed.WorkerManager.{ExecutePendingWrites, StartOfNewTick}
import com.bharatsim.engine.distributed.actors.Barrier.BarrierFinished
import com.bharatsim.engine.distributed.actors.DistributedTickLoop._
import com.bharatsim.engine.distributed.{CborSerializable, ContextData}
import com.bharatsim.engine.execution.simulation.PostSimulationActions
import com.bharatsim.engine.execution.tick.{PostTickActions, PreTickActions}
import com.bharatsim.engine.graph.neo4j.BatchWriteNeo4jProvider
import org.neo4j.driver.Bookmark

import scala.concurrent.duration.Duration.Inf
import scala.concurrent.duration.{Duration, DurationInt}
import scala.concurrent.{Await, ExecutionContext, Future}

class DistributedTickLoop(
    simulationContext: Context,
    preTickActions: PreTickActions,
    postTickActions: PostTickActions,
    postSimulationActions: PostSimulationActions
) {

  class Tick(actorContext: ActorContext[Command], currentTick: Int, bookmarks: List[DBBookmark])
      extends AbstractBehavior(actorContext) {
    private implicit val seconds: Timeout = 3.seconds
    private implicit val scheduler: Scheduler = context.system.scheduler
    private val workerList = fetchAvailableWorkers(context)
    simulationContext.graphProvider
      .asInstanceOf[BatchWriteNeo4jProvider]
      .setBookmarks(bookmarks)
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
            preTickActions.execute(currentTick)
            notifyWorkersNewTick()
            new WorkDistributorV2(workerList, context.self, simulationContext)
              .init(context)
            Behaviors.same
          }

        case ReadsFinished =>
          executePendingWrites()
          Behaviors.same

        case BarrierReply(r) =>
          context.log.info("Finished executing pending writes")
          r match {
            case BarrierFinished(bookmarks) => {
              postTickActions.execute()
              Tick(currentTick + 1, bookmarks)
            }
          }
          postTickActions.execute()
          Tick(currentTick + 1)
      }

    private def executePendingWrites(): Unit = {
      context.log.info("Started executing pending writes")
      val f = simulationContext.graphProvider
        .asInstanceOf[BatchWriteNeo4jProvider]
        .executePendingWrites(actorContext.system)
      val bookmark = Await.result(f, Inf)
      val adaptedToBarrierReply = actorContext.messageAdapter(response => BarrierReply(response))
      val barrier =
        context.spawn(
          Barrier(0, Some(workerList.length), adaptedToBarrierReply, List(DBBookmark(bookmark.values()))),
          "write-barrier"
        )
      workerList.foreach(worker => worker ! ExecutePendingWrites(barrier))
    }

    private def notifyWorkersNewTick(): Unit = {
      Await.result(
        Future.foldLeft(
          workerList.map(worker =>
            worker.ask((replyTo: ActorRef[ContextUpdateDone]) => {
              val updatedContext =
                ContextData(simulationContext.getCurrentStep, simulationContext.activeInterventionNames)
              StartOfNewTick(updatedContext, bookmarks, replyTo)
            })
          )
        )()((_, _) => ())(ExecutionContext.global),
        Inf
      )
    }

    private def fetchAvailableWorkers(context: ActorContext[Command]) = {
      Await.result(
        context.system.receptionist.ask[Receptionist.Listing](replyTo => Receptionist.find(workerServiceKey, replyTo)),
        Duration.Inf
      ) match {
        case workerServiceKey.Listing(listings) => listings.toArray
      }
    }
  }

  object Tick {
    def apply(currentTick: Int, bookmarks: List[DBBookmark] = List.empty): Behavior[Command] = {
      Behaviors.setup(context => new Tick(context, currentTick, bookmarks))
    }
  }
}

object DistributedTickLoop {

  sealed trait Command extends CborSerializable
  case object CurrentTick extends Command
  case object ReadsFinished extends Command
  case class BarrierReply(message: Barrier.Reply) extends Command

  case class ContextUpdateDone() extends CborSerializable
}

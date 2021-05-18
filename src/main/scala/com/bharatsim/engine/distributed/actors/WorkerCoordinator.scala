package com.bharatsim.engine.distributed.actors

import akka.actor.typed.receptionist.Receptionist
import akka.actor.typed.scaladsl.ActorContext
import akka.actor.typed.scaladsl.AskPattern.Askable
import akka.actor.typed.{ActorRef, Scheduler}
import akka.util.Timeout
import com.bharatsim.engine.distributed.Guardian.workerServiceId
import com.bharatsim.engine.distributed.WorkerManager.{ExecutePendingWrites, StartOfNewTick}
import com.bharatsim.engine.distributed.actors.DistributedTickLoop.ContextUpdateDone
import com.bharatsim.engine.distributed.{ContextData, DBBookmark, WorkerManager}
import com.bharatsim.engine.{ApplicationConfigFactory, Context}

import scala.concurrent.duration.Duration.Inf
import scala.concurrent.duration.{Duration, DurationInt}
import scala.concurrent.{Await, ExecutionContext, Future}

class WorkerCoordinator() {
  private implicit val askTimeout: Timeout = ApplicationConfigFactory.config.askTimeout.seconds
  private var workerList = List.empty[ActorRef[WorkerManager.Command]]

  def initTick(
      context: ActorContext[DistributedTickLoop.Command],
      simContext: Context,
      bookmarks: List[DBBookmark]
  ) = {
    workerList = fetchAvailableWorkers(context)
    notifyNewTickToWorkers(context, workerList, simContext, bookmarks)
  }
  def workerCount = workerList.size

  def startWork(
      context: ActorContext[DistributedTickLoop.Command],
      simContext: Context,
      barrier: ActorRef[Barrier.Request]
  ): Unit = {

    val distributableWork =
      new DistributableWork(simContext.agentLabels.toList, simContext.simulationConfig.countBatchSize)
    if (distributableWork.isComplete) {
      context.self ! DistributedTickLoop.ExecuteWrites
      return
    }

    val actor = context.spawn(
      WorkDistributor(barrier, workerList, distributableWork),
      s"distributor-${simContext.getCurrentStep}"
    )
    actor ! WorkDistributor.Start
  }

  private def fetchAvailableWorkers(
      context: ActorContext[DistributedTickLoop.Command]
  ): List[ActorRef[WorkerManager.Command]] = {
    implicit val scheduler: Scheduler = context.system.scheduler
    Await.result(
      context.system.receptionist.ask[Receptionist.Listing](replyTo => Receptionist.find(workerServiceId, replyTo)),
      Duration.Inf
    ) match {
      case workerServiceId.Listing(listings) => listings.toList
    }
  }

  private def notifyNewTickToWorkers(
      context: ActorContext[_],
      workerList: List[ActorRef[WorkerManager.Command]],
      simulationContext: Context,
      bookmarks: List[DBBookmark]
  ): Unit = {
    implicit val scheduler: Scheduler = context.system.scheduler
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

  def notifyExecuteWrites(barrier: ActorRef[Barrier.Request]): Unit = {
    workerList.foreach(worker => worker ! ExecutePendingWrites(barrier))
  }
}

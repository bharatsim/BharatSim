package com.bharatsim.engine.distributed.engineMain

import akka.actor.typed.receptionist.Receptionist
import akka.actor.typed.scaladsl.AskPattern.Askable
import akka.actor.typed.{ActorRef, ActorSystem, Scheduler}
import akka.util.Timeout
import com.bharatsim.engine.distributed.engineMain.DistributedTickLoop.StartOfNewTickAck
import com.bharatsim.engine.distributed.worker.WorkerActor
import com.bharatsim.engine.distributed.worker.WorkerActor.{
  ExecutePendingWrites,
  StartOfNewTick,
  Shutdown,
  workerServiceId
}
import com.bharatsim.engine.distributed.{ContextData, DBBookmark}
import com.bharatsim.engine.{ApplicationConfigFactory, Context}

import scala.concurrent.duration.Duration.Inf
import scala.concurrent.duration.{Duration, DurationInt}
import scala.concurrent.{Await, ExecutionContext, Future}

class WorkerCoordinator() {
  private implicit val askTimeout: Timeout = ApplicationConfigFactory.config.askTimeout.seconds
  private var workerList = List.empty[ActorRef[WorkerActor.Command]]

  def initTick(
      system: ActorSystem[_],
      simContext: Context,
      bookmarks: List[DBBookmark]
  ) = {
    workerList = fetchAvailableWorkers(system)
    notifyNewTickToWorkers(system, workerList, simContext, bookmarks)
  }
  def workerCount = workerList.size

  def startWork(workDistributor: ActorRef[WorkDistributor.Command]): Unit = {
    workDistributor ! WorkDistributor.Start(workerList)
  }

  def executeWrites(barrier: ActorRef[Barrier.Request]): Unit = {
    workerList.foreach(worker => worker ! ExecutePendingWrites(barrier))
  }

  private def fetchAvailableWorkers(system: ActorSystem[_]): List[ActorRef[WorkerActor.Command]] = {
    implicit val scheduler: Scheduler = system.scheduler
    Await.result(
      system.receptionist.ask[Receptionist.Listing](replyTo => Receptionist.find(workerServiceId, replyTo)),
      Duration.Inf
    ) match {
      case workerServiceId.Listing(listings) => listings.toList
    }
  }

  private def notifyNewTickToWorkers(
      system: ActorSystem[_],
      workerList: List[ActorRef[WorkerActor.Command]],
      simContext: Context,
      bookmarks: List[DBBookmark]
  ): Unit = {
    implicit val scheduler: Scheduler = system.scheduler
    val updatedContext = ContextData(simContext.getCurrentStep, simContext.activeInterventionNames)
    Await.result(
      Future.foldLeft(
        workerList.map(worker =>
          worker.ask((replyTo: ActorRef[StartOfNewTickAck]) => {
            StartOfNewTick(updatedContext, bookmarks, replyTo)
          })
        )
      )()((_, _) => ())(ExecutionContext.global),
      Inf
    )
  }

  def triggerShutdown(reason: String, origin: ActorRef[_]) = {
    workerList.foreach(_ ! Shutdown(reason, origin))
  }

}

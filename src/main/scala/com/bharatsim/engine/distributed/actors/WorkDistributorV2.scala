package com.bharatsim.engine.distributed.actors

import akka.actor.Address
import akka.actor.typed.scaladsl.{ActorContext, Behaviors}
import akka.actor.typed.{ActorRef, Behavior}
import com.bharatsim.engine.Context
import com.bharatsim.engine.distributed.{CborSerializable, WorkerManager}
import com.bharatsim.engine.distributed.WorkerManager.{NoWork, Work}
import com.bharatsim.engine.distributed.actors.WorkDistributorV2._

import scala.annotation.tailrec
import scala.collection.mutable

class WorkDistributorV2(
    workers: Array[ActorRef[WorkerManager.Command]],
    tickLoop: ActorRef[DistributedTickLoop.Command],
    simulationContext: Context
) {
  private val labels = simulationContext.agentLabels.iterator
  private val limit = simulationContext.simulationConfig.countBatchSize
  private val finishedWorkers = mutable.HashSet.empty[Address]

  def self(label: String, skip: Int, endOfWork: Boolean = false): Behavior[Command] =
    Behaviors.setup { context =>
      def handleStart(label: String, skip: Int, limit: Int): Behavior[Command] = {
        @tailrec
        def sendToAll(
                       workerIterator: Iterator[ActorRef[WorkerManager.Command]],
                       skipRec: Int
        ): Behavior[Command] = {
          if (workerIterator.hasNext) {
            val cur = workerIterator.next()
            cur ! Work(label, skipRec, limit, context.self)
            sendToAll(workerIterator, skipRec + limit)
          } else {
            self(label, skipRec)
          }
        }

        sendToAll(workers.iterator, skip)
      }

      def handleFetch(
          label: String,
          skip: Int,
          limit: Int,
          endOfWork: Boolean,
          sendTo: ActorRef[WorkerManager.Command]
      ): Behavior[Command] = {
        if (endOfWork) {
          sendTo ! NoWork(context.self)
          Behaviors.same
        } else {
          sendTo ! Work(label, skip, limit, context.self)
          self(label, skip + limit)
        }
      }

      Behaviors.receiveMessage {
        case Start() =>
          handleStart(label, skip, limit)
        case ExhaustedFor(exhausted) =>
          if(!endOfWork) {
            if (label == exhausted) {
              if (labels.hasNext) self(labels.next(), 0)
              else self("", 0, endOfWork = true)
            } else Behaviors.same
          } else Behaviors.same
        case FetchWork(sendTo: ActorRef[WorkerManager.Command]) =>
          handleFetch(label, skip, limit, endOfWork, sendTo)
        case AckNoWork(from) =>
          finishedWorkers.add(from.path.address)
          if (finishedWorkers.size == workers.length) {
            tickLoop ! DistributedTickLoop.AllWorkFinished
            Behaviors.stopped
          } else Behaviors.same
      }
    }

  def init(system: ActorContext[_]): Unit = {
    if (labels.hasNext) {
      val actor = system.spawn(self(labels.next(), 0), "distributor")
      actor ! WorkDistributorV2.Start()
    } else {
      tickLoop ! DistributedTickLoop.AllWorkFinished
    }
  }
}

object WorkDistributorV2 {
  sealed trait Command extends CborSerializable

  case class Start() extends Command
  case class FetchWork(sendTo: ActorRef[WorkerManager.Command]) extends Command
  case class ExhaustedFor(label: String) extends Command
  case class AckNoWork(from: ActorRef[WorkerManager.Command]) extends Command
}

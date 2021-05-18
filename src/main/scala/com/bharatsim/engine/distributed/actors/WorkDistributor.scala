package com.bharatsim.engine.distributed.actors

import akka.actor.typed.scaladsl.{AbstractBehavior, ActorContext, Behaviors}
import akka.actor.typed.{ActorRef, Behavior}
import com.bharatsim.engine.distributed.WorkerManager.Work
import com.bharatsim.engine.distributed.actors.Barrier.{NotifyOnBarrierFinished, WorkFinished}
import com.bharatsim.engine.distributed.actors.WorkDistributor._
import com.bharatsim.engine.distributed.{CborSerializable, WorkerManager}

class WorkDistributor(
    context: ActorContext[Command],
    barrier: ActorRef[Barrier.Request],
    workers: List[ActorRef[WorkerManager.Command]],
    work: DistributableWork
) extends AbstractBehavior(context) {

  private def sendWorkToAll(): Behavior[Command] = {
    val pendingWork = workers.foldLeft(work) { (pendingWork, worker) =>
      worker ! Work(pendingWork.agentLabel, pendingWork.finishedCount, pendingWork.batchSize, context.self)
      pendingWork.nextBatch
    }
    new WorkDistributor(context, barrier, workers, pendingWork)
  }

  override def onMessage(msg: Command): Behavior[Command] = {
    msg match {
      case Start => {
        barrier ! NotifyOnBarrierFinished(context.messageAdapter(_ => Stop))
        sendWorkToAll()
      }
      case AgentLabelExhausted(exhausted) =>
        if (!work.isComplete && work.agentLabel == exhausted) {
          val nextWork = work.nextAgentLabel
          new WorkDistributor(context, barrier, workers, nextWork)
        } else Behaviors.same

      case FetchWork(sendTo: ActorRef[WorkerManager.Command]) =>
        if (work.isComplete) {
          barrier ! WorkFinished()
          Behaviors.same
        } else {
          sendTo ! Work(work.agentLabel, work.finishedCount, work.batchSize, context.self)
          val nextWork = work.nextBatch
          new WorkDistributor(context, barrier, workers, nextWork)
        }
      case Stop => Behaviors.stopped
    }
  }

}

object WorkDistributor {
  def apply(
      barrier: ActorRef[Barrier.Request],
      workers: List[ActorRef[WorkerManager.Command]],
      work: DistributableWork
  ): Behavior[Command] =
    Behaviors.setup { context =>
      new WorkDistributor(context, barrier, workers, work)
    }

  sealed trait Command extends CborSerializable
  case object Start extends Command
  case class FetchWork(sendTo: ActorRef[WorkerManager.Command]) extends Command
  case class AgentLabelExhausted(label: String) extends Command
  case object Stop extends Command
}

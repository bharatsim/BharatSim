package com.bharatsim.engine.distributed.engineMain

import akka.actor.typed.scaladsl.{AbstractBehavior, ActorContext, Behaviors}
import akka.actor.typed.{ActorRef, Behavior}
import com.bharatsim.engine.distributed.CborSerializable
import com.bharatsim.engine.distributed.engineMain.Barrier.{NotifyOnBarrierFinished, WorkErrored, WorkFinished}
import com.bharatsim.engine.distributed.engineMain.WorkDistributor._
import com.bharatsim.engine.distributed.worker.WorkerActor
import com.bharatsim.engine.distributed.worker.WorkerActor.Work

class WorkDistributor(
    context: ActorContext[Command],
    barrier: ActorRef[Barrier.Request],
    work: DistributableWork
) extends AbstractBehavior(context) {

  private def sendWorkToAll(workers: List[ActorRef[WorkerActor.Command]]): Behavior[Command] = {
    val pendingWork = workers.foldLeft(work) { (pendingWork, worker) =>
      worker ! Work(pendingWork.agentLabel, pendingWork.finishedCount, pendingWork.batchSize, context.self)
      pendingWork.nextBatch
    }
    new WorkDistributor(context, barrier, pendingWork)
  }

  override def onMessage(msg: Command): Behavior[Command] = {
    msg match {
      case Start(workers) => {
        barrier ! NotifyOnBarrierFinished(context.messageAdapter(_ => Stop))
        sendWorkToAll(workers)
      }
      case AgentLabelExhausted(label) =>
        if (!work.isComplete && work.agentLabel == label) {
          val nextWork = work.nextAgentLabel
          new WorkDistributor(context, barrier, nextWork)
        } else Behaviors.same

      case FetchWork(sendTo: ActorRef[WorkerActor.Command]) =>
        if (work.isComplete) {
          barrier ! WorkFinished()
          Behaviors.same
        } else {
          sendTo ! Work(work.agentLabel, work.finishedCount, work.batchSize, context.self)
          val nextWork = work.nextBatch
          new WorkDistributor(context, barrier, nextWork)
        }
      case WorkFailed(error, origin) =>
        barrier ! WorkErrored(error, origin)
        Behaviors.same
      case Stop => Behaviors.stopped
    }
  }

}

object WorkDistributor {
  def apply(
      barrier: ActorRef[Barrier.Request],
      work: DistributableWork
  ): Behavior[Command] =
    Behaviors.setup { context =>
      new WorkDistributor(context, barrier, work)
    }

  sealed trait Command extends CborSerializable
  case class Start(workers: List[ActorRef[WorkerActor.Command]]) extends Command
  case class FetchWork(sendTo: ActorRef[WorkerActor.Command]) extends Command
  case class AgentLabelExhausted(label: String) extends Command
  case class WorkFailed(error: String, origin: ActorRef[_]) extends Command
  case object Stop extends Command
}

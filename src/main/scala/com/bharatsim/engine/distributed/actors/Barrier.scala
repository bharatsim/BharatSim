package com.bharatsim.engine.distributed.actors

import akka.actor.typed.scaladsl.Behaviors
import akka.actor.typed.{ActorRef, Behavior}
import com.bharatsim.engine.distributed.WorkerManager.ChildrenFinished
import com.bharatsim.engine.distributed.{CborSerializable, WorkerManager}

class Barrier {}

object Barrier {
  def apply(finished: Int, work: Option[Int], tick: ActorRef[WorkerManager.Command], distributorV2: ActorRef[WorkDistributorV2.Command]): Behavior[Command] =
    Behaviors.receiveMessage[Command] {
      case UnitOfWorkFinished =>
        if (work.isDefined && finished + 1 == work.get) handleEnd(tick, distributorV2)
        else Barrier(finished + 1, work, tick, distributorV2)
      case SetWorkCount(count) =>
        if(finished >= count) handleEnd(tick, distributorV2)
        else Barrier(finished, Some(count), tick, distributorV2)
    }

  private def handleEnd(tick: ActorRef[WorkerManager.Command], distributorV2: ActorRef[WorkDistributorV2.Command]): Behavior[Command] = {
    tick ! ChildrenFinished(distributorV2)
    Behaviors.stopped
  }

  sealed trait Command extends CborSerializable
  case object UnitOfWorkFinished extends Command
  case class SetWorkCount(count: Int) extends Command
}

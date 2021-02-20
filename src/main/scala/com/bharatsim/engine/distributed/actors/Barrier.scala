package com.bharatsim.engine.distributed.actors

import akka.actor.typed.{ActorRef, Behavior}
import akka.actor.typed.scaladsl.Behaviors
import com.bharatsim.engine.distributed.CborSerializable
import com.bharatsim.engine.distributed.actors.DistributedTickLoop.AllWorkFinished

class Barrier {}

object Barrier {
  def apply(finished: Int, work: Option[Int], tick: ActorRef[DistributedTickLoop.Command]): Behavior[Command] =
    Behaviors.receiveMessage[Command] {
      case UnitOfWorkFinished =>
        if (work.isDefined && finished + 1 == work.get) handleEnd(tick)
        else Barrier(finished + 1, work, tick)
      case SetWorkCount(count) =>
        if(finished >= count) handleEnd(tick)
        else Barrier(finished, Some(count), tick)
    }

  private def handleEnd(tick: ActorRef[DistributedTickLoop.Command]): Behavior[Command] = {
    tick ! AllWorkFinished
    Behaviors.stopped
  }

  sealed trait Command extends CborSerializable
  case object UnitOfWorkFinished extends Command
  case class SetWorkCount(count: Int) extends Command
}

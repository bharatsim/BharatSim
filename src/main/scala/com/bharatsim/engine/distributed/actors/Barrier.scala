package com.bharatsim.engine.distributed.actors

import akka.actor.typed.scaladsl.Behaviors
import akka.actor.typed.{ActorRef, Behavior}
import com.bharatsim.engine.distributed.CborSerializable

object Barrier {
  def apply(finished: Int, workSize: Option[Int], replyFinishTo: ActorRef[BarrierFinished]): Behavior[Request] =
    Behaviors.receiveMessage[Request] {
      case WorkFinished() =>
        if (workSize.isDefined && finished + 1 == workSize.get) handleEnd(replyFinishTo)
        else Barrier(finished + 1, workSize, replyFinishTo)
      case SetWorkCount(count) =>
        if (finished >= count) handleEnd(replyFinishTo)
        else Barrier(finished, Some(count), replyFinishTo)
      case Die() => Behaviors.stopped
    }

  private def handleEnd(replyFinishTo: ActorRef[BarrierFinished]): Behavior[Request] = {
    replyFinishTo ! BarrierFinished()
    Behaviors.stopped
  }

  sealed trait Request extends CborSerializable
  case class WorkFinished() extends Request
  case class SetWorkCount(count: Int) extends Request
  case class Die() extends Request

  sealed trait Reply extends CborSerializable
  case class BarrierFinished() extends Reply
}

package com.bharatsim.engine.distributed.actors

import akka.actor.typed.scaladsl.Behaviors
import akka.actor.typed.{ActorRef, Behavior}
import com.bharatsim.engine.distributed.{CborSerializable, DBBookmark}
import org.neo4j.driver.Bookmark

object Barrier {
  def apply(
      finished: Int,
      workSize: Option[Int],
      replyFinishTo: ActorRef[BarrierFinished],
      bookmarks: List[DBBookmark] = List.empty
  ): Behavior[Request] =
    Behaviors.receiveMessage[Request] {
      case WorkFinished(bookmark) =>
        if (workSize.isDefined && finished + 1 == workSize.get) handleEnd(replyFinishTo, bookmark :: bookmarks)
        else Barrier(finished + 1, workSize, replyFinishTo, bookmark :: bookmarks)
      case SetWorkCount(count) =>
        if (finished >= count) handleEnd(replyFinishTo, bookmarks)
        else Barrier(finished, Some(count), replyFinishTo)
      case Die() => Behaviors.stopped
    }

  private def handleEnd(replyFinishTo: ActorRef[BarrierFinished], bookmarks: List[DBBookmark]): Behavior[Request] = {
    replyFinishTo ! BarrierFinished(bookmarks)
    Behaviors.stopped
  }

  sealed trait Request extends CborSerializable
  case class WorkFinished(bookmark: DBBookmark) extends Request
  case class SetWorkCount(count: Int) extends Request
  case class Die() extends Request

  sealed trait Reply extends CborSerializable
  case class BarrierFinished(bookmarks: List[DBBookmark]) extends Reply
}

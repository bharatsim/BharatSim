package com.bharatsim.engine.distributed.engineMain

import akka.actor.typed.scaladsl.Behaviors
import akka.actor.typed.{ActorRef, Behavior}
import com.bharatsim.engine.distributed.{CborSerializable, DBBookmark}
import com.typesafe.scalalogging.LazyLogging

object Barrier extends LazyLogging {
  def apply(
      finished: Int,
      workSize: Int,
      replyFinishTo: ActorRef[Reply],
      bookmarks: List[DBBookmark] = List.empty,
      additionalListeners: List[ActorRef[BarrierFinished]] = List.empty
  ): Behavior[Request] =
    Behaviors.receiveMessage[Request] {
      case WorkFinished(maybeBookmark) =>
        val newBookmarks = if (maybeBookmark.isDefined) maybeBookmark.get :: bookmarks else bookmarks
        val newFinishedCount = finished + 1
        if (newFinishedCount == workSize) {
          replyFinishTo ! BarrierFinished(newBookmarks)
          additionalListeners.foreach(_ ! BarrierFinished(newBookmarks))
          Behaviors.stopped
        } else Barrier(newFinishedCount, workSize, replyFinishTo, newBookmarks, additionalListeners)

      case NotifyOnBarrierFinished(toRef) =>
        Barrier(finished, workSize, replyFinishTo, bookmarks, toRef :: additionalListeners)

      case WorkErrored(error, origin) =>
        replyFinishTo ! BarrierAborted(error, origin)
        Behaviors.stopped

      case Stop() => Behaviors.stopped
    }
  sealed trait Request extends CborSerializable
  case class WorkFinished(result: Option[DBBookmark] = None) extends Request
  case class WorkErrored(error: String, origin: ActorRef[_]) extends Request
  case class Stop() extends Request
  case class NotifyOnBarrierFinished(toRef: ActorRef[BarrierFinished]) extends Request
  sealed trait Reply extends CborSerializable
  case class BarrierFinished(bookmarks: List[DBBookmark]) extends Reply
  case class BarrierAborted(error: String, origin: ActorRef[_]) extends Reply
}

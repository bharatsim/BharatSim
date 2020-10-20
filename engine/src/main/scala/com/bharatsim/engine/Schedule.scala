package com.bharatsim.engine

import com.bharatsim.engine.exception.{CyclicScheduleException, ScheduleOutOfBoundsException}
import com.bharatsim.engine.models.Node
import com.bharatsim.engine.utils.Utils

import scala.collection.mutable
import scala.reflect.ClassTag

class Schedule(val period: ScheduleUnit, val unit: ScheduleUnit) {

  val _schedule: mutable.HashMap[Integer, Any] = new mutable.HashMap[Integer, Any]()

  private def checkOutOfBound(to: Int) = {
    val maxBound = (period.steps / unit.steps - 1)
    if (to > maxBound) throw new ScheduleOutOfBoundsException("Schedule exceeds period limit of :(0-" + maxBound + ")")
  }

  private def getKey(step: Int): Int = {
    (step / unit.steps) % (period.steps / unit.steps)
  }

  def add[T <: Node : ClassTag](from: Int, to: Int): Schedule = {
    checkOutOfBound(to)
    for (i <- from to to) {
      _schedule.put(i, Utils.fetchClassName[T])
    }
    this
  }

  private def isResolvable(schedule: Schedule): Boolean = {
    if (schedule == this) return false
    schedule._schedule.valuesIterator.toSet
      .forall(_ match {
        case _: String               => true
        case childSchedule: Schedule => isResolvable(childSchedule);
      })
  };

  def add(schedule: Schedule, from: Int, to: Int): Schedule = {
    if (!isResolvable(schedule)) throw new CyclicScheduleException("The schedule is creating cyclic reference")
    for (i <- from to to) {
      _schedule.put(i, schedule)
    }
    this
  }

  def getForStep(step: Int): String = {
    val scheduleValue = _schedule(getKey(step))
    scheduleValue match {
      case place: String      => place
      case schedule: Schedule => schedule.getForStep(step)
    }
  }
}

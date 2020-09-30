package com.bharatsim.engine

import com.bharatsim.engine.exception.ScheduleOutOfBoundsException

import scala.collection.mutable

class Schedule(val period: ScheduleUnit, val unit: ScheduleUnit) {

  val _schedule: mutable.HashMap[Integer, Any] = new mutable.HashMap[Integer, Any]()

  private def checkOutOfBound(to: Int) = {
    val maxBound = (period.steps / unit.steps - 1)
    if (to > maxBound) throw new ScheduleOutOfBoundsException("Schedule exceeds period limit of :(0-" + maxBound + ")")
  }

  private def getKey(step: Int): Int = {
    (step / unit.steps) % (period.steps / unit.steps)
  }

  def add(place: String, from: Int, to: Int): Schedule = {
    checkOutOfBound(to)
    for (i <- from to to) {
      _schedule.put(i, place)
    }
    this
  }

  def add(schedule: Schedule, from: Int, to: Int): Schedule = {
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

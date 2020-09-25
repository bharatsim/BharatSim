package com.bharatsim.engine

import scala.collection.mutable

class Schedule(val period: ScheduleUnit, val unit: ScheduleUnit) {

  val _schedule: mutable.HashMap[Integer, Any] = new mutable.HashMap[Integer, Any]()

  def add(place: String, from: Int, to: Int): Schedule = {
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

  private def getKey(step: Int): Int = {
    (step / unit.steps) % (period.steps / unit.steps)
  }

  def getForStep(step: Int): String = {
    val scheduleValue = _schedule(getKey(step))
    scheduleValue match {
      case place: String      => place
      case schedule: Schedule => schedule.getForStep(step)
    }
  }
}

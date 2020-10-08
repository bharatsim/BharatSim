package com.bharatsim.engine.dsl

import com.bharatsim.engine.{Schedule, ScheduleUnit}

object SyntaxHelpers {

  implicit class ScheduleMaker(x: (ScheduleUnit, ScheduleUnit)) {
    def add(place: String, from: Int, to: Int): Schedule = {
      val s = new Schedule(x._1, x._2)
      s.add(place, from, to)
    }

    def add(schedule: Schedule, from: Int, to: Int): Schedule = {
      val s = new Schedule(x._1, x._2)
      s.add(schedule, from, to)
    }
  }

}

package com.bharatsim.engine.dsl

import com.bharatsim.engine.models.Node
import com.bharatsim.engine.{Schedule, ScheduleUnit}

import scala.reflect.ClassTag

object SyntaxHelpers {

  implicit class ScheduleMaker(x: (ScheduleUnit, ScheduleUnit)) {
    def add[T <: Node : ClassTag](from: Int, to: Int): Schedule = {
      val s = new Schedule(x._1, x._2)
      s.add[T](from, to)
    }

    def add(schedule: Schedule, from: Int, to: Int): Schedule = {
      val s = new Schedule(x._1, x._2)
      s.add(schedule, from, to)
    }
  }

}

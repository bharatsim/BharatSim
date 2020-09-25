package com.bharatsim.engine

import scala.collection.mutable.ListBuffer

class Schedules {
  type Matcher = (Agent, Context) => Boolean
  case class ScheduleMatcher(schedule: Schedule, matcher: Matcher)

  val schedules: ListBuffer[ScheduleMatcher] = new ListBuffer[ScheduleMatcher]

  def addSchedule(schedule: Schedule, matcher: Matcher): Unit = {
    schedules.addOne(ScheduleMatcher(schedule, matcher))
  }

  def getSchedule(agent: Agent, context: Context): Option[Schedule] = {
    val scheduleMatcher = schedules.find(_.matcher(agent, context))
    if (scheduleMatcher.isDefined) Some(scheduleMatcher.get.schedule) else None
  }

}

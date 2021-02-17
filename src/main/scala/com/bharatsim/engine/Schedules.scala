package com.bharatsim.engine

import com.bharatsim.engine.models.Agent

import scala.collection.mutable.ListBuffer

private[engine] class Schedules {
  type Matcher = (Agent, Context) => Boolean

  private case class ScheduleMatcher(schedule: Schedule, matcher: Matcher, priority: Int)

  private val schedules: ListBuffer[ScheduleMatcher] = new ListBuffer[ScheduleMatcher]

  def add(schedule: Schedule, matcher: Matcher, priority: Int): Unit = {
    schedules.addOne(ScheduleMatcher(schedule, matcher, priority))
  }

  def get(agent: Agent, context: Context): Option[Schedule] = {
    val scheduleMatcher = schedules.filter(_.matcher(agent, context)).sortBy(_.priority)
    if (scheduleMatcher.nonEmpty) Some(scheduleMatcher.head.schedule) else None
  }

}

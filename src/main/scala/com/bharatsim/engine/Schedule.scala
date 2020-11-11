package com.bharatsim.engine

import com.bharatsim.engine.exception.{CyclicScheduleException, ScheduleOutOfBoundsException}
import com.bharatsim.engine.models.Node
import com.bharatsim.engine.utils.Utils

import scala.collection.mutable
import scala.reflect.ClassTag

/**
  * @param period is length of schedule in terms of [[com.bharatsim.engine.ScheduleUnit ScheduleUnit]]
  * @param unit is length of each unit of period in term of [[com.bharatsim.engine.ScheduleUnit ScheduleUnit]].
  */
class Schedule(val period: ScheduleUnit, val unit: ScheduleUnit) {

  private val _schedule: mutable.HashMap[Integer, Either[String, Schedule]] = mutable.HashMap.empty

  private def checkOutOfBound(to: Int) = {
    val maxBound = (period.steps / unit.steps - 1)
    if (to > maxBound) throw new ScheduleOutOfBoundsException("Schedule exceeds period limit of :(0-" + maxBound + ")")
  }

  private def getKey(step: Int): Int = {
    (step / unit.steps) % (period.steps / unit.steps)
  }

  /**
    * Add entry to a schedule for a [[com.bharatsim.engine.models.Node Node]]
    *
    * @param from start of the entry in schedule. This is zero based i.e. the very first entry will start with zero.
    * @param to  end of the entry in schedule.
    * @tparam T is type of the Node the entry is associated with
    * @return the same instance of Schedule for chaining purposes.
    * @throws com.bharatsim.engine.exception.ScheduleOutOfBoundsException  when a schedule is exceeding its period bound.
    */
  def add[T <: Node: ClassTag](from: Int, to: Int): Schedule = {
    checkOutOfBound(to)
    for (i <- from to to) {
      _schedule.put(i, Left(Utils.fetchClassName[T]))
    }
    this
  }

  /**
    * Add entry to a schedule for another [[com.bharatsim.engine.Schedule Schedule]]
    *
    * @param schedule is sub-schedule that entry is associated with
    * @param from start of the entry in schedule. This is zero based i.e. the very first entry will start with zero.
    * @param to  end of the entry in schedule.
    * @return the same instance of Schedule for chaining purposes.
    * @throws com.bharatsim.engine.exception.CyclicScheduleException  when a schedule is creating cyclic reference.
    * @throws com.bharatsim.engine.exception.ScheduleOutOfBoundsException  when a schedule is exceeding its period bound.
    */
  def add(schedule: Schedule, from: Int, to: Int): Schedule = {
    if (!isResolvable(schedule)) throw new CyclicScheduleException("The schedule is creating cyclic reference")
    for (i <- from to to) {
      _schedule.put(i, Right(schedule))
    }
    this
  }

  /**
    *  Gets the type of Node associated with schedule for given simulation step by resolving all the sub-schedule.
    *
    * @param step a simulation step
    * @return the type of Node associated with schedule entry for given simulation step.
    */
  def getForStep(step: Int): String = {
    val scheduleValue = _schedule(getKey(step))
    scheduleValue match {
      case Left(place)     => place
      case Right(schedule) => schedule.getForStep(step)
    }
  }

  private def isResolvable(schedule: Schedule): Boolean = {
    if (schedule == this) return false
    schedule._schedule.valuesIterator.toSet
      .forall(_ match {
        case Left(_)              => true
        case Right(childSchedule) => isResolvable(childSchedule)
      })
  }
}

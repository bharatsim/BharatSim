package com.bharatsim.examples.epidemiology.sir

import com.bharatsim.engine.ScheduleUnit

object Parameters {

  final val numberOfTicksInADay: Int = 2
  final val dt: Double = 1.0/numberOfTicksInADay

  final val myTick: ScheduleUnit = new ScheduleUnit(1)
  final val myDay: ScheduleUnit = new ScheduleUnit(myTick * numberOfTicksInADay)

  final val initialInfectedFraction = 0.1

  final val beta: Double = 0.1
  final val gamma: Double = 1.0/7
}

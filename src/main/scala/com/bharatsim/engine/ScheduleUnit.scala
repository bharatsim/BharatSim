package com.bharatsim.engine

/**
  * Unit use to define the [[com.bharatsim.engine.Schedule Schedule]]
  *  A ScheduleUnit defined in terms of number of simulation steps
  * @param steps number of simulation steps
  */
class ScheduleUnit(val steps: Int) {

  /**
    * Performs multiplication on number of simulation steps for current unit and multiplier
    *
    * @param times is multiplier
    * @return a number of simulation steps after multiplication
    */
  def *(times: Int): Int = {
    steps * times
  }
}

case object Hour extends ScheduleUnit(1)
case object Day extends ScheduleUnit(Hour * 24)
case object Week extends ScheduleUnit(Day * 7)

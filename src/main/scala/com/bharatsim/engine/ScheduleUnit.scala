package com.bharatsim.engine

class ScheduleUnit(val steps: Int) {
  def *(times: Int): Int = {
    steps * times
  }
}

case object Hour extends ScheduleUnit(1)
case object Day extends ScheduleUnit(Hour * 24)
case object Week extends ScheduleUnit(Day * 7)

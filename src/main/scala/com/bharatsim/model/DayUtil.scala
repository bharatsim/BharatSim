package com.bharatsim.model

object DayUtil {
  private final val numberOfHoursInADay: Int = 24

  def isEOD(step: Int): Boolean = step % numberOfHoursInADay == 0
}

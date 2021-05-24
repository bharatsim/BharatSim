package com.bharatsim.model12Hr

object DayUtil {
  private final val numberOfHoursInADay: Int = 24

  def isEOD(step: Int): Boolean = step % numberOfHoursInADay == 0
}

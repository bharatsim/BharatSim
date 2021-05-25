package com.bharatsim.examples.epidemiology.tenCompartmentalModel

object DayUtil {
  private final val numberOfHoursInADay: Int = 24

  def isEOD(step: Int): Boolean = step % numberOfHoursInADay == 0
}

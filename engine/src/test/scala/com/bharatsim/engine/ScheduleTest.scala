package com.bharatsim.engine

import com.bharatsim.engine.exception.ScheduleOutOfBoundsException
import org.scalatest.funsuite.AnyFunSuite
import org.scalatest.matchers.should.Matchers

class ScheduleTest extends AnyFunSuite with Matchers {

  test("should be able to define schedule for places") {
    val employeeSchedule = new Schedule(new ScheduleUnit(Hour * 5), Hour)
      .add("Home", 0, 1)
      .add("Office", 2, 3)
      .add("Home", 4, 4)

    val expectedScheduleFor10Hr =
      List("Home", "Home", "Office", "Office", "Home", "Home", "Home", "Office", "Office", "Home")

    val actualScheduleFor10Hr = (0 to 9).map(employeeSchedule.getForStep).toList

    actualScheduleFor10Hr should equal(expectedScheduleFor10Hr)
  }

  test("should be able to define schedule in terms of other schedules") {
    val TwoHrDay = new ScheduleUnit(Hour * 2)
    val TwoDayWeek = new ScheduleUnit(TwoHrDay * 2)
    val employeeScheduleOnWeekDays = new Schedule(TwoHrDay, Hour)
      .add("Office", 0, 1)

    val employeeScheduleOnWeekEnd = new Schedule(TwoHrDay, Hour)
      .add("Party", 0, 1)

    val employeeSchedule = new Schedule(TwoDayWeek, TwoHrDay)
      .add(employeeScheduleOnWeekDays, 0, 0)
      .add(employeeScheduleOnWeekEnd, 1, 1)

    val expectedScheduleFor2weeks = List("Office", "Office", "Party", "Party", "Office", "Office", "Party", "Party")

    val actualScheduleFor2weeks = (0 to 7).map(employeeSchedule.getForStep).toList
    actualScheduleFor2weeks should equal(expectedScheduleFor2weeks)
  }

  test("should throw exception when extending schedule beyond specified period") {
    val employeeSchedule = new Schedule(Day, Hour)

    val exception = the[ScheduleOutOfBoundsException] thrownBy employeeSchedule.add("Home", 0, 25)
    exception.getMessage shouldBe "Schedule exceeds period limit of :(0-23)"
  }
}

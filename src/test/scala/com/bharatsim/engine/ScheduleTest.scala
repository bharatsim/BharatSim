package com.bharatsim.engine

import com.bharatsim.engine.exception.{CyclicScheduleException, ScheduleOutOfBoundsException}
import com.bharatsim.engine.models.{Agent, Node}
import org.scalatest.funsuite.AnyFunSuite
import org.scalatest.matchers.should.Matchers

class ScheduleTest extends AnyFunSuite with Matchers {

  private case class MockAgent() extends Agent

  private case class MockHouse() extends Node

  private case class MockOffice() extends Node

  test("should be able to define schedule for places") {
    val employeeSchedule = new Schedule(new ScheduleUnit(Hour * 5), Hour)
      .add[MockHouse](0, 1)
      .add[MockOffice](2, 3)
      .add[MockHouse](4, 4)

    val expectedScheduleFor10Hr =
      List(
        "MockHouse", "MockHouse", "MockOffice", "MockOffice",
        "MockHouse", "MockHouse", "MockHouse", "MockOffice",
        "MockOffice", "MockHouse"
      )

    val actualScheduleFor10Hr = (0 to 9).map(employeeSchedule.getForStep).toList

    actualScheduleFor10Hr should equal(expectedScheduleFor10Hr)
  }

  test("should be able to define schedule in terms of other schedules") {
    val TwoHrDay = new ScheduleUnit(Hour * 2)
    val TwoDayWeek = new ScheduleUnit(TwoHrDay * 2)
    val employeeScheduleOnWeekDays = new Schedule(TwoHrDay, Hour)
      .add[MockOffice](0, 1)

    val employeeScheduleOnWeekEnd = new Schedule(TwoHrDay, Hour)
      .add[MockHouse](0, 1)

    val employeeSchedule = new Schedule(TwoDayWeek, TwoHrDay)
      .add(employeeScheduleOnWeekDays, 0, 0)
      .add(employeeScheduleOnWeekEnd, 1, 1)

    val expectedScheduleFor2weeks = List(
      "MockOffice", "MockOffice", "MockHouse", "MockHouse",
      "MockOffice", "MockOffice", "MockHouse", "MockHouse"
    )

    val actualScheduleFor2weeks = (0 to 7).map(employeeSchedule.getForStep).toList
    actualScheduleFor2weeks should equal(expectedScheduleFor2weeks)
  }

  test("should throw exception when extending schedule beyond specified period") {
    val employeeSchedule = new Schedule(Day, Hour)

    val exception = the[ScheduleOutOfBoundsException] thrownBy employeeSchedule.add[MockHouse](0, 25)
    exception.getMessage shouldBe "Schedule exceeds period limit of :(0-23)"
  }

  test("should throw exception when cyclic schedule is added") {
    val schedule1 = new Schedule(Day, Hour)
    val schedule2 = new Schedule(Day, Hour)
    val schedule3 = new Schedule(Day, Hour)

    val selfReferenceError = the[CyclicScheduleException] thrownBy schedule1.add(schedule1, 0, 8)

    schedule1.add(schedule2, 0, 8)
    schedule3.add(schedule1, 0, 8)

    val cyclicScheduleError = the[CyclicScheduleException] thrownBy schedule2.add(schedule3, 0, 8)

    the[CyclicScheduleException] thrownBy schedule2.add(schedule1, 0, 8)

    cyclicScheduleError.getMessage shouldBe "The schedule is creating cyclic reference"
    selfReferenceError.getMessage shouldBe "The schedule is creating cyclic reference"
  }
}

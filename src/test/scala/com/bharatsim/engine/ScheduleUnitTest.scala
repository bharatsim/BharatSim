package com.bharatsim.engine

import org.scalatest.funsuite.AnyFunSuite
import org.scalatest.matchers.should.Matchers

class ScheduleUnitTest extends AnyFunSuite with Matchers {

  test("should multiply steps when * is called") {
    val two = new ScheduleUnit(2)
    two * 3 shouldBe 6
    two * 5 shouldBe 10
  }

  test("should have predefined Hour,Day and Week") {
    Hour.steps shouldBe 1
    Day.steps shouldBe Hour * 24
    Week.steps shouldBe Day * 7
  }
}

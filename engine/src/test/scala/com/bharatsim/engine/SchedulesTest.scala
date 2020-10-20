package com.bharatsim.engine

import com.bharatsim.engine.models.Agent
import org.mockito.MockitoSugar
import org.scalatest.funsuite.AnyFunSuite
import org.scalatest.matchers.should.Matchers

class SchedulesTest extends AnyFunSuite with Matchers with MockitoSugar {

  test("should be able to add multiple schedule") {
    val employeeSchedule = mock[Schedule]
    val studentSchedule = mock[Schedule]
    val context = mock[Context]
    val employee = mock[Agent]
    val student = mock[Agent]

    val schedules = new Schedules()

    schedules.addSchedule(employeeSchedule, (agent: Agent, _: Context) => { agent == employee })
    schedules.addSchedule(studentSchedule, (agent: Agent, _: Context) => { agent == student })

    schedules.getSchedule(employee, context).get shouldBe employeeSchedule
    schedules.getSchedule(student, context).get shouldBe studentSchedule
  }

  test("should get None  when no schedule matches") {
    val employee = mock[Agent]
    val context = mock[Context]

    val schedules = new Schedules()

    schedules.getSchedule(employee, context) shouldBe None
  }

  test("should get first matching schedule when multiple schedules are matching") {
    val employeeSchedule1 = mock[Schedule]
    val employeeSchedule2 = mock[Schedule]
    val context = mock[Context]
    val employee = mock[Agent]
    val schedules = new Schedules()
    val employeeMatcher = spyLambda((agent: Agent, _: Context) => { agent == employee })

    schedules.addSchedule(employeeSchedule1, employeeMatcher)
    schedules.addSchedule(employeeSchedule2, employeeMatcher)

    schedules.getSchedule(employee, context).get shouldBe employeeSchedule1
    verify(employeeMatcher)(employee, context)
  }
}

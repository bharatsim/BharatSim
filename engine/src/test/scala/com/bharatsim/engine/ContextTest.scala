package com.bharatsim.engine

import org.scalatest.funsuite.AnyFunSuite
import org.scalatest.matchers.should.Matchers._

class ContextTest extends AnyFunSuite {

  test("should set dynamics") {
    val covidDisease = new Dynamics
    val context = new Context
    context.setDynamics(covidDisease)

    context.dynamics shouldBe covidDisease
  }
}

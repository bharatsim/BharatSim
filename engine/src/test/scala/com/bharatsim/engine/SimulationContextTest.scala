package com.bharatsim.engine

import org.scalatest.funsuite.AnyFunSuite
import org.scalatest.matchers.should.Matchers._

class SimulationContextTest extends AnyFunSuite {

  test("should be able to set steps") {
    val context = new SimulationContext
    context.setSteps(10);
    context.simulationSteps shouldBe 10
  }

  test("should be able to set and get current step") {
    val context = new SimulationContext
    context.setCurrentStep(5);
    context.getCurrentStep() shouldBe 5
  }
}

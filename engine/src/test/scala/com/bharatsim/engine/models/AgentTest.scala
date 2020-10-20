package com.bharatsim.engine.models

import com.bharatsim.engine.Context
import org.scalatest.funsuite.AnyFunSuite
import org.scalatest.matchers.should.Matchers._

class AgentTest extends AnyFunSuite {

  test("Should be instanceOf Node") {
    val agent = new Agent()
    agent shouldBe a[Node]
  }

  test("Should allow to add multiple behaviours") {
    val mockBehaviour1 = (context: Context) => {}
    val mockBehaviour2 = (context: Context) => {}
    val agent = new Agent()
    agent.addBehaviour(mockBehaviour1)
    agent.addBehaviour(mockBehaviour2)

    agent.behaviours should have length 2
    agent.behaviours.head shouldBe mockBehaviour1
    agent.behaviours(1) shouldBe mockBehaviour2
  }

}

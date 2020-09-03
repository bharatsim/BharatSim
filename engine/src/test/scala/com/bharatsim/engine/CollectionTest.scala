package com.bharatsim.engine

import org.scalatest.funsuite.AnyFunSuite
import org.scalatest.matchers.should.Matchers._

class CollectionTest extends AnyFunSuite {
  val agent1 = new Agent
  val agent2 = new Agent

  test("Should be able to add and get all") {
    val agents = new Collection[Agent]
    agents.add(agent1)
    agents.add(agent2)
    agents.getAll

    val agentsList = agents.getAll.toList
    agentsList should have length 2
  }

  test("Should be able to getById") {
    val agents = new Collection[Agent]
    agents.add(agent1)
    agents.add(agent2)
    val agent = agents.get(agent1.id)

    agent shouldBe agent1
  }
}

package com.bharatsim.engine

import org.mockito.MockitoSugar
import org.scalatest.funsuite.AnyFunSuite
import org.scalatest.matchers.should.Matchers._

class NetworkTest extends AnyFunSuite with MockitoSugar {

  test("Should have Id") {
    val network = new Network()
    network.id shouldBe a[Int]
  }

  test("Should be able to give Id") {
    val network = new Network(1)
    network.id shouldBe 1
  }

  test("Should connect agents") {
    val network = new Network()
    val agent1 = spy(new Agent)
    val agent2 = spy(new Agent)

    network.connectAgent(agent1)
    network.connectAgent(agent2)

    verify(agent1).setNetwork(network)
    verify(agent2).setNetwork(network)
  }

  test("Should getMembers") {
    val network = new Network
    val agent1 = new Agent
    val agent2 = new Agent

    network.connectAgent(agent1)
    network.connectAgent(agent2)

    val members = network.getMembers().toList
    members should have length 2
    members should contain(agent1);
    members should contain(agent2);
  }

  test(
    "Should disconnect from previous network when switching to new network"
  ) {
    val previousNetwork = new Network
    val agent1 = new Agent
    val agent2 = new Agent
    val newNetwork = new Network
    previousNetwork.connectAgent(agent1)
    previousNetwork.connectAgent(agent2)

    newNetwork.connectAgent(agent1)

    val membersOfPreviousNetwork = previousNetwork.getMembers().toList
    val membersOfNewNetwork = newNetwork.getMembers().toList

    membersOfPreviousNetwork should not contain (agent1)
    membersOfPreviousNetwork should contain(agent2)
    membersOfNewNetwork should contain(agent1)
  }

  test("Should not connect to same network twice") {
    val network = new Network
    val agent1 = spy(new Agent)

    network.connectAgent(agent1)
    network.connectAgent(agent1)

    val members = network.getMembers().toList

    members should contain(agent1)
    members should have length 1
  }

}

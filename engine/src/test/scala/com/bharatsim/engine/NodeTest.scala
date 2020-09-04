package com.bharatsim.engine

import org.mockito.MockitoSugar
import org.scalatest.funsuite.AnyFunSuite
import org.scalatest.matchers.should.Matchers._

class NodeTest extends AnyFunSuite with MockitoSugar {

  test("Should have Id") {
    val node = new Node()
    node.id shouldBe a[Int]
  }

  test("Should be able to give Id") {
    val node = new Node(1)
    node.id shouldBe 1
  }

  test("Should connect nodes to each other with unidirectional connection") {
    val node1 = new Node
    val node2 = new Node

    node1.unidirectionalConnect("uniConnect", node2)

    val node1Connections = node1.getConnections("uniConnect").toList
    val node2Connections = node2.getConnections("uniConnect").toList
    node1Connections should contain(node2)
    node1Connections should have length 1
    node2Connections shouldBe empty
  }

  test("Should connect nodes to each other with bidirectional connection") {
    val node1 = new Node
    val node2 = new Node

    node1.bidirectionalConnect("connects", node2)

    val node1Connections = node1.getConnections("connects").toList
    val node2Connections = node2.getConnections("connects").toList
    node1Connections should contain(node2)
    node1Connections should have length 1
    node2Connections should contain(node1)
    node2Connections should have length 1
  }

  test("Should be able to connect more than one node with same relation") {
    val node1 = new Node
    val node2 = new Node
    val node3 = new Node

    node1.unidirectionalConnect("connects", node2)
    node1.unidirectionalConnect("connects", node3)

    val node1Connections = node1.getConnections("connects").toList

    node1Connections should contain(node2)
    node1Connections should contain(node3)
    node1Connections should have length 2
  }

  test("Should be able to disconnect unidirectional connect") {
    val node1 = new Node
    val node2 = new Node

    node1.unidirectionalConnect("connects", node2)
    node1.disconnect("connects", node2)

    val node1Connections = node1.getConnections("connects").toList

    node1Connections shouldBe empty
  }

  test("Should be able to disconnect bidirectional connect") {
    val node1 = new Node
    val node2 = new Node

    node1.bidirectionalConnect("connects", node2)
    node1.disconnect("connects", node2)

    val node1Connections = node1.getConnections("connects").toList
    val node2Connections = node2.getConnections("connects").toList

    node1Connections shouldBe empty
    node2Connections shouldBe empty
  }
}

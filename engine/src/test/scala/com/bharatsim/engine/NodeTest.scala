package com.bharatsim.engine

import com.bharatsim.engine.graph.GraphProvider
import org.mockito.MockitoSugar
import org.scalatest.funsuite.AnyFunSuite
import org.scalatest.matchers.should.Matchers._

class NodeTest extends AnyFunSuite with MockitoSugar {

  test("Should have Id") {
    val node = new Node()
    node.id shouldBe a[Int]
  }

  test("Should connect nodes to each other with unidirectional connection") {
    implicit val graphProvider: GraphProvider = mock[GraphProvider]
    val node1 = new Node
    node1.setId(1)
    val node2 = new Node
    node2.setId(2)

    node1.unidirectionalConnect("uniConnect", node2)

    verify(graphProvider).createRelationship(1, "uniConnect", 2)
  }

  test("Should connect nodes to each other with bidirectional connection") {
    implicit val graphProvider: GraphProvider = mock[GraphProvider]

    val node1 = new Node
    node1.setId(1)
    val node2 = new Node
    node2.setId(2)

    node1.bidirectionalConnect("connects", node2)

    verify(graphProvider).createRelationship(1, "connects", 2)
    verify(graphProvider).createRelationship(2, "connects", 1)
  }

  test("Should be able to disconnect unidirectional connect") {
    implicit val graphProvider: GraphProvider = mock[GraphProvider]

    val node1 = new Node
    node1.setId(1)
    val node2 = new Node
    node2.setId(2)

    node1.disconnect("connects", node2)

    verify(graphProvider).deleteRelationship(1, "connects", 2)
  }
}

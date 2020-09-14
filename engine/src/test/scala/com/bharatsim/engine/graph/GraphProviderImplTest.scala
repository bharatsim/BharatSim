package com.bharatsim.engine.graph

import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec

class GraphProviderImplTest extends AnyWordSpec with Matchers {
  "fetchNode" when {
    "provided with only label" should {
      "return any node with given label" in {
        val graphProvider = new GraphProviderImpl()
        graphProvider.createNode("Person", ("name", "Ramesh"))

        val someNode = graphProvider.fetchNode("Person")
        someNode.isDefined shouldBe true
        someNode.get.apply("name").get shouldBe "Ramesh"
      }

      "return no node if label doesnt exist" in {
        val graphProvider = new GraphProviderImpl()

        val someNode = graphProvider.fetchNode("Person")
        someNode.isDefined shouldBe false
      }
    }

    "provided with label and properties" should {
      "return any node matching with label and properties" in {
        val graphProvider = new GraphProviderImpl()
        graphProvider.createNode("Person", Map(("name", "Suresh"), ("age", 32)))


        val result1 = graphProvider.fetchNode("Person", Map(("name", "Suresh"), ("age", 32)))
        val result2 = graphProvider.fetchNode("Person", Map(("name", "Suresh")))
        val result3 = graphProvider.fetchNode("Person", Map(("age", 32)))

        result1.isDefined shouldBe true
        result2.isDefined shouldBe true
        result3.isDefined shouldBe true
        result1.get.apply("name").get shouldBe "Suresh"
        result2.get.apply("name").get shouldBe "Suresh"
        result3.get.apply("name").get shouldBe "Suresh"
      }

      "return no node if label and properties does not match with any node" in {
        val graphProvider = new GraphProviderImpl()
        graphProvider.createNode("Person", Map(("name", "Suresh"), ("age", 32)))


        val result1 = graphProvider.fetchNode("Person", Map(("name", "Suresh"), ("age", 32)))
        val result2 = graphProvider.fetchNode("Person", Map(("name", "Suresh")))
        val result3 = graphProvider.fetchNode("Person", Map(("age", 32)))

        result1.isDefined shouldBe true
        result2.isDefined shouldBe true
        result3.isDefined shouldBe true
        result1.get.apply("name").get shouldBe "Suresh"
        result2.get.apply("name").get shouldBe "Suresh"
        result3.get.apply("name").get shouldBe "Suresh"
      }
    }
  }

  "fetchNodes" when {
    "provided with only label" should {
      "return all nodes with given label" in {
        val graphProvider = new GraphProviderImpl()

        graphProvider.createNode("Person", ("name", "Ramesh"), ("age", 23))
        graphProvider.createNode("Person", ("name", "Suresh"), ("age", 36))

        val nodes = graphProvider.fetchNodes("Person")
        nodes.size shouldBe 2
      }

      "return empty list if label doesnt exist" in {
        val graphProvider = new GraphProviderImpl()

        graphProvider.createNode("Person", ("name", "Ramesh"), ("age", 23))
        graphProvider.createNode("Person", ("name", "Suresh"), ("age", 36))

        val nodes = graphProvider.fetchNodes("School")
        nodes.size shouldBe 0
      }
    }

    "provided with label and properties" should {
      "return any node matching with label and properties" in {
        val graphProvider = new GraphProviderImpl()

        graphProvider.createNode("Person", ("name", "Ramesh"), ("age", 23))
        graphProvider.createNode("Person", ("name", "Suresh"), ("age", 36))

        val nodes = graphProvider.fetchNodes("Person", ("name", "Ramesh"))
        nodes.size shouldBe 1
        nodes.head.apply("age").get shouldBe 23
      }

      "return no node if label and properties does not match with any node" in {
        val graphProvider = new GraphProviderImpl()

        graphProvider.createNode("Person", ("name", "Ramesh"), ("age", 23))
        graphProvider.createNode("Person", ("name", "Suresh"), ("age", 36))

        val nodes = graphProvider.fetchNodes("Person", ("name", "Aadesh"))
        nodes.size shouldBe 0
      }
    }
  }

  "fetchNeighboursOf" when {
    "node exists" should {
      "return nodes with provided labels" in {
        val graphProvider = new GraphProviderImpl()
        val node1 = graphProvider.createNode("Person", ("name", "Ramesh"))
        val node2 = graphProvider.createNode("Person", ("name", "Suresh"))
        val node3 = graphProvider.createNode("Person", ("name", "Harish"))
        graphProvider.createRelationship("OWES", node1, node2)
        graphProvider.createRelationship("OWES", node1, node3)

        val neighbors = graphProvider.fetchNeighborsOf(node1, "OWES")
        neighbors.size shouldBe 2
      }

      "return unique nodes always" in {
        val graphProvider = new GraphProviderImpl()
        val node1 = graphProvider.createNode("Person", ("name", "Ramesh"))
        val node2 = graphProvider.createNode("Person", ("name", "Suresh"))
        val node3 = graphProvider.createNode("Person", ("name", "Harish"))
        graphProvider.createRelationship("OWES", node1, node2)
        graphProvider.createRelationship("OWES", node1, node3)
        graphProvider.createRelationship("LIKES", node1, node3)

        val neighbors = graphProvider.fetchNeighborsOf(node1, "OWES", "LIKES")
        neighbors.size shouldBe 2
      }

      "return no nodes if relationship with label does not exist" in {
        val graphProvider = new GraphProviderImpl()
        val node1 = graphProvider.createNode("Person", ("name", "Ramesh"))
        val node2 = graphProvider.createNode("Person", ("name", "Suresh"))
        val node3 = graphProvider.createNode("Person", ("name", "Harish"))
        graphProvider.createRelationship("OWES", node1, node2)
        graphProvider.createRelationship("OWES", node1, node3)

        val neighbors = graphProvider.fetchNeighborsOf(node1, "KNOWS")
        neighbors.size shouldBe 0
      }
    }

    "node does not exist" should {
      "return no nodes" in {
        val graphProvider = new GraphProviderImpl()
        val node1 = graphProvider.createNode("Person", ("name", "Ramesh"))
        val node2 = graphProvider.createNode("Person", ("name", "Suresh"))
        val node3 = graphProvider.createNode("Person", ("name", "Harish"))
        graphProvider.createRelationship("OWES", node1, node2)
        graphProvider.createRelationship("OWES", node1, node3)

        val neighbors = graphProvider.fetchNeighborsOf(10, "OWES")
        neighbors.size shouldBe 0
      }
    }
  }
}

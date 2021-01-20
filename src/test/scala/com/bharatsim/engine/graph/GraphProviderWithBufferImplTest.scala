package com.bharatsim.engine.graph

import com.bharatsim.engine.basicConversions.encoders.DefaultEncoders._
import com.bharatsim.engine.graph.custom.GraphProviderWithBufferImpl
import com.bharatsim.engine.graph.ingestion.{GraphData, Relation}
import com.bharatsim.engine.graph.patternMatcher.MatchCondition._
import com.bharatsim.engine.testModels.{TestCitizen, TestHome}
import org.mockito.MockitoSugar
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec

class GraphProviderWithBufferImplTest extends AnyWordSpec with Matchers with MockitoSugar {
  "fetchNode" when {
    "provided with only label" should {
      "return any node with given label" in {
        val graphProvider = new GraphProviderWithBufferImpl()
        graphProvider.createNode("Person", ("name", "Ramesh"))
        graphProvider.syncBuffers()
        val someNode = graphProvider.fetchNode("Person")
        someNode.isDefined shouldBe true
        someNode.get.apply("name").get shouldBe "Ramesh"
      }

      "return no node if label doesnt exist" in {
        val graphProvider = new GraphProviderWithBufferImpl()

        val someNode = graphProvider.fetchNode("Person")
        someNode.isDefined shouldBe false
      }

      "return node form read buffer" in {
        val graphProvider = new GraphProviderWithBufferImpl()
        graphProvider.createNode("Person", ("name", "Ramesh"))
        val someNode = graphProvider.fetchNode("Person")
        someNode.isDefined shouldBe false
      }
    }

    "provided with label and properties" should {
      "return any node matching with label and properties" in {
        val graphProvider = new GraphProviderWithBufferImpl()
        graphProvider.createNode("Person", Map(("name", "Suresh"), ("age", 32)))
        graphProvider.syncBuffers()

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
        val graphProvider = new GraphProviderWithBufferImpl()
        graphProvider.createNode("Person", Map(("name", "Suresh"), ("age", 32)))
        graphProvider.syncBuffers()

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
        val graphProvider = new GraphProviderWithBufferImpl()

        graphProvider.createNode("Person", ("name", "Ramesh"), ("age", 23))
        graphProvider.createNode("Person", ("name", "Suresh"), ("age", 36))
        graphProvider.syncBuffers()

        val nodes = graphProvider.fetchNodes("Person")
        nodes.size shouldBe 2
      }

      "return empty list if label doesnt exist" in {
        val graphProvider = new GraphProviderWithBufferImpl()

        graphProvider.createNode("Person", ("name", "Ramesh"), ("age", 23))
        graphProvider.createNode("Person", ("name", "Suresh"), ("age", 36))
        graphProvider.syncBuffers()

        val nodes = graphProvider.fetchNodes("School")
        nodes.size shouldBe 0
      }
    }

    "provided with label and properties" should {
      "return any node matching with label and properties" in {
        val graphProvider = new GraphProviderWithBufferImpl()

        graphProvider.createNode("Person", ("name", "Ramesh"), ("age", 23))
        graphProvider.createNode("Person", ("name", "Suresh"), ("age", 36))
        graphProvider.syncBuffers()

        val nodes = graphProvider.fetchNodes("Person", ("name", "Ramesh"))
        nodes.size shouldBe 1
        nodes.head.apply("age").get shouldBe 23
      }

      "return no node if label and properties does not match with any node" in {
        val graphProvider = new GraphProviderWithBufferImpl()

        graphProvider.createNode("Person", ("name", "Ramesh"), ("age", 23))
        graphProvider.createNode("Person", ("name", "Suresh"), ("age", 36))
        graphProvider.syncBuffers()

        val nodes = graphProvider.fetchNodes("Person", ("name", "Aadesh"))
        nodes.size shouldBe 0
      }
    }

    "provided with label and matcher" should {
      "return any node matching with label and conditions" in {
        val graphProvider = new GraphProviderWithBufferImpl()

        graphProvider.createNode("Person", ("name", "Ramesh"), ("age", 23))
        graphProvider.createNode("Person", ("name", "Suresh"), ("age", 36))
        graphProvider.syncBuffers()

        val nodes = graphProvider.fetchNodes("Person", "name" equ "Ramesh")
        nodes.size shouldBe 1
        nodes.head.apply("age").get shouldBe 23
      }

      "return no node if label and conditions does not match with any node" in {
        val graphProvider = new GraphProviderWithBufferImpl()

        graphProvider.createNode("Person", ("name", "Ramesh"), ("age", 23))
        graphProvider.createNode("Person", ("name", "Suresh"), ("age", 36))
        graphProvider.syncBuffers()

        val nodes = graphProvider.fetchNodes("Person", "name" equ "Aadesh")
        nodes.size shouldBe 0
      }
    }
  }

  "fetchCount" when {
    "label exists in store" should {
      "return count of nodes matching the condition" in {
        val graphProvider = new GraphProviderWithBufferImpl()

        graphProvider.createNode("Person", ("age", 34))
        graphProvider.createNode("Person", ("age", 24))
        graphProvider.createNode("Person", ("age", 12))
        graphProvider.createNode("Person", ("age", 67))
        graphProvider.createNode("Person", ("age", 34))
        graphProvider.syncBuffers()

        graphProvider.fetchCount("Person", "age" equ 34) shouldBe 2
        graphProvider.fetchCount("Person", "age" lt 100) shouldBe 5
        graphProvider.fetchCount("Person", "age" lte 34) shouldBe 4
        graphProvider.fetchCount("Person", "age" gt 50) shouldBe 1
      }
    }

    "label does not exists" should {
      "return 0" in {
        val graphProvider = new GraphProviderWithBufferImpl()

        graphProvider.createNode("Person", ("name", "Suresh"))
        graphProvider.syncBuffers()

        graphProvider.fetchCount("Vector", "spreadProbability" equ 0.34) shouldBe 0
      }
    }
  }

  "fetchNeighboursOf" when {
    "node exists" should {
      "return nodes with provided labels" in {
        val graphProvider = new GraphProviderWithBufferImpl()
        val node1 = graphProvider.createNode("Person", ("name", "Ramesh"))
        val node2 = graphProvider.createNode("Person", ("name", "Suresh"))
        val node3 = graphProvider.createNode("Person", ("name", "Harish"))
        graphProvider.createRelationship(node1, "OWES", node2)
        graphProvider.createRelationship(node1, "OWES", node3)
        graphProvider.syncBuffers()

        val neighbors = graphProvider.fetchNeighborsOf(node1, "OWES")
        neighbors.size shouldBe 2
      }

      "return unique nodes always" in {
        val graphProvider = new GraphProviderWithBufferImpl()
        val node1 = graphProvider.createNode("Person", ("name", "Ramesh"))
        val node2 = graphProvider.createNode("Person", ("name", "Suresh"))
        val node3 = graphProvider.createNode("Person", ("name", "Harish"))
        graphProvider.createRelationship(node1, "OWES", node2)
        graphProvider.createRelationship(node1, "OWES", node3)
        graphProvider.createRelationship(node1, "LIKES", node3)
        graphProvider.syncBuffers()

        val neighbors = graphProvider.fetchNeighborsOf(node1, "OWES", "LIKES")
        neighbors.size shouldBe 2
      }

      "return no nodes if relationship with label does not exist" in {
        val graphProvider = new GraphProviderWithBufferImpl()
        val node1 = graphProvider.createNode("Person", ("name", "Ramesh"))
        val node2 = graphProvider.createNode("Person", ("name", "Suresh"))
        val node3 = graphProvider.createNode("Person", ("name", "Harish"))
        graphProvider.createRelationship(node1, "OWES", node2)
        graphProvider.createRelationship(node1, "OWES", node3)
        graphProvider.syncBuffers()

        val neighbors = graphProvider.fetchNeighborsOf(node1, "KNOWS")
        neighbors.size shouldBe 0
      }
    }

    "node does not exist" should {
      "return no nodes" in {
        val graphProvider = new GraphProviderWithBufferImpl()
        val node1 = graphProvider.createNode("Person", ("name", "Ramesh"))
        val node2 = graphProvider.createNode("Person", ("name", "Suresh"))
        val node3 = graphProvider.createNode("Person", ("name", "Harish"))
        graphProvider.createRelationship(node1, "OWES", node2)
        graphProvider.createRelationship(node1, "OWES", node3)
        graphProvider.syncBuffers()

        val neighbors = graphProvider.fetchNeighborsOf(10, "OWES")
        neighbors.size shouldBe 0
      }
    }
  }

  "neighborCount" when {
    "provided count condition" should {
      "return count of neighbors matching the condition" in {
        val graphProvider = new GraphProviderWithBufferImpl

        val home = graphProvider.createNode("Home", ("homeId", 1))
        val person1 = graphProvider.createNode("Person", ("id", 1), ("age", 22))
        val person2 = graphProvider.createNode("Person", ("id", 2), ("age", 23))
        val person3 = graphProvider.createNode("Person", ("id", 3), ("age", 23))
        graphProvider.createRelationship(home, "HOUSES", person1)
        graphProvider.createRelationship(home, "HOUSES", person2)
        graphProvider.createRelationship(home, "HOUSES", person3)
        graphProvider.syncBuffers()

        graphProvider.neighborCount(home, "HOUSES", "age" lt 23) shouldBe 1
      }
    }
  }

  "updateProps" when {
    "node exists" should {
      "update existing or add new props" in {
        val graphProvider = new GraphProviderWithBufferImpl()
        val nodeId = graphProvider.createNode("Person", ("name", "Rajesh"))

        graphProvider.updateNode(nodeId, ("name", "Suresh"), ("age", 23))
        graphProvider.syncBuffers()

        val maybeNode = graphProvider.fetchNode("Person", Map(("name", "Suresh")))
        maybeNode.isDefined shouldBe true
        maybeNode.get.apply("age").get shouldBe 23
        maybeNode.get.apply("name").get shouldBe "Suresh"
      }
      "not reflect update on read before sync" in {
        val graphProvider = new GraphProviderWithBufferImpl()
        val nodeId = graphProvider.createNode("Person", ("name", "Rajesh"))

        graphProvider.updateNode(nodeId, ("name", "Suresh"), ("age", 23))

        val maybeNode = graphProvider.fetchNode("Person", Map(("name", "Suresh")))
        maybeNode.isDefined shouldBe false
      }
    }
  }

  "deleteNode" should {
    "remove node from the store" in {
      val graphProvider = new GraphProviderWithBufferImpl
      val nodeId = graphProvider.createNode("Person", ("name", "Ramesh"))
      graphProvider.syncBuffers()

      graphProvider.fetchNodes("Person").size shouldBe 1

      graphProvider.deleteNode(nodeId)
      graphProvider.syncBuffers()

      graphProvider.fetchNodes("Person").size shouldBe 0
    }
    "not reflect delete on read before sync" in {
      val graphProvider = new GraphProviderWithBufferImpl
      val nodeId = graphProvider.createNode("Person", ("name", "Ramesh"))
      graphProvider.syncBuffers()
      graphProvider.fetchNodes("Person").size shouldBe 1

      graphProvider.deleteNode(nodeId)

      graphProvider.fetchNodes("Person").size shouldBe 1
      graphProvider.syncBuffers()

      graphProvider.fetchNodes("Person").size shouldBe 0

    }
    "delete all the associations of node from other nodes" in {
      val graphProvider = new GraphProviderWithBufferImpl
      val nodeId1 = graphProvider.createNode("Person", ("name", "Ramesh"))
      val nodeId2 = graphProvider.createNode("Person", ("name", "Suresh"))
      graphProvider.createRelationship(nodeId1, "KNOWS", nodeId2)
      graphProvider.deleteNode(nodeId2)
      graphProvider.syncBuffers()

      val people = graphProvider.fetchNodes("Person").toList
      people.size shouldBe 1
      graphProvider.fetchNeighborsOf(nodeId1, "KNOWS").toList.size shouldBe 0
    }
  }

  "deleteNodes" should {
    "remove all nodes matching the label and properties" in {
      val graphProvider = new GraphProviderWithBufferImpl

      graphProvider.createNode("Person", ("name", "Ramesh"), ("age", 24))
      graphProvider.createNode("Person", ("name", "Suresh"), ("age", 24))
      graphProvider.createNode("Person", ("name", "Harish"), ("age", 24))
      graphProvider.createNode("Person", ("name", "Girish"), ("age", 26))

      graphProvider.deleteNodes("Person", Map(("age", 24)))

      graphProvider.syncBuffers()

      val personList = graphProvider.fetchNodes("Person")
      personList.size shouldBe 1
      personList.head.apply("name").get shouldBe "Girish"
    }

    "not reflect deleteNodes on read before sync" in {
      val graphProvider = new GraphProviderWithBufferImpl

      graphProvider.createNode("Person", ("name", "Ramesh"), ("age", 24))
      graphProvider.createNode("Person", ("name", "Suresh"), ("age", 24))
      graphProvider.createNode("Person", ("name", "Harish"), ("age", 24))
      graphProvider.createNode("Person", ("name", "Girish"), ("age", 26))
      graphProvider.syncBuffers()

      graphProvider.deleteNodes("Person", Map(("age", 24)))

      val personList = graphProvider.fetchNodes("Person")
      personList.size shouldBe 4
    }
  }

  "deleteRelationship" should {
    "remove relationship between the nodes" in {
      val graphProvider = new GraphProviderWithBufferImpl
      val from = graphProvider.createNode("Person", ("name", "Ramesh"))
      val to = graphProvider.createNode("Person", ("name", "Suresh"))
      graphProvider.createRelationship(from, "OWES", to)
      graphProvider.syncBuffers()

      graphProvider.fetchNeighborsOf(from, "OWES").size shouldBe 1

      graphProvider.deleteRelationship(from, "OWES", to)
      graphProvider.syncBuffers()

      graphProvider.fetchNeighborsOf(from, "OWES").size shouldBe 0
    }

    "not reflect deleteRelationships on read before sync" in {
      val graphProvider = new GraphProviderWithBufferImpl
      val from = graphProvider.createNode("Person", ("name", "Ramesh"))
      val to = graphProvider.createNode("Person", ("name", "Suresh"))
      graphProvider.createRelationship(from, "OWES", to)
      graphProvider.syncBuffers()

      graphProvider.fetchNeighborsOf(from, "OWES").size shouldBe 1

      graphProvider.deleteRelationship(from, "OWES", to)

      graphProvider.fetchNeighborsOf(from, "OWES").size shouldBe 1

      graphProvider.syncBuffers()
      graphProvider.fetchNeighborsOf(from, "OWES").size shouldBe 0

    }
  }

  "deleteAll" should {
    "remove all the nodes and relationships from the store" in {
      val graphProvider = new GraphProviderWithBufferImpl
      val node1 = graphProvider.createNode("Person", ("age", 34))
      val node2 = graphProvider.createNode("Person", ("age", 34))
      graphProvider.createNode("Person", ("age", 34))
      graphProvider.createNode("Person", ("age", 34))
      graphProvider.createNode("Person", ("age", 34))
      graphProvider.createRelationship(node1, "OWES", node2)

      graphProvider.deleteAll()
      graphProvider.syncBuffers()

      graphProvider.fetchNodes("Person").size shouldBe 0
      graphProvider.fetchNeighborsOf(node1, "OWES").size shouldBe 0
    }

    "not reflect deleteRelationships on read before sync" in {
      val graphProvider = new GraphProviderWithBufferImpl
      val node1 = graphProvider.createNode("Person", ("age", 34))
      val node2 = graphProvider.createNode("Person", ("age", 34))
      graphProvider.createNode("Person", ("age", 34))
      graphProvider.createNode("Person", ("age", 34))
      graphProvider.createNode("Person", ("age", 34))
      graphProvider.createRelationship(node1, "OWES", node2)
      graphProvider.syncBuffers()

      graphProvider.deleteAll()

      graphProvider.fetchNodes("Person").size shouldBe 5
      graphProvider.fetchNeighborsOf(node1, "OWES").size shouldBe 1
      graphProvider.syncBuffers()
      graphProvider.fetchNodes("Person").size shouldBe 0
      graphProvider.fetchNeighborsOf(node1, "OWES").size shouldBe 0
    }
  }

  "ingestFromCsv" should {
    "create node from CSV file" in {
      val filePath = "src/test/scala/com/bharatsim/engine/graph/sample.csv"
      val mapper = Some((map: Map[String, String]) => {
        val age = map("age").toInt
        val graphData = GraphData()
        graphData.addNode(map("id").toInt, TestCitizen(age))
        graphData
      })

      val graphProvider = new GraphProviderWithBufferImpl
      graphProvider.ingestFromCsv(filePath, mapper)
      val nodes = graphProvider.fetchNodes("TestCitizen").toList
      nodes should have length 2
      nodes.map(_.getParams("age")) should contain theSameElementsAs List(25, 35)
    }

    "create relations from CSV file" in {
      val filePath = "src/test/scala/com/bharatsim/engine/graph/sample.csv"
      val mapper = Some((map: Map[String, String]) => {
        val nodeId = map("id").toInt
        val age = map("age").toInt

        val citizenNode = TestCitizen(age)
        val homeId = map("house_id").toInt
        val home = TestHome()
        val staysAt = Relation[TestCitizen, TestHome](nodeId, "STAYS_AT", homeId)
        val memberOf = Relation[TestHome, TestCitizen](homeId, "HOUSES", nodeId)
        val graphData = GraphData()
        graphData.addRelations(staysAt, memberOf)
        graphData.addNode(nodeId, citizenNode)
        graphData.addNode(homeId, home)
        graphData
      })

      val graphProvider = new GraphProviderWithBufferImpl
      graphProvider.ingestFromCsv(filePath, mapper)
      val house = graphProvider.fetchNode("TestHome").toList.head
      val citizensId = graphProvider.fetchNeighborsOf(house.Id, "HOUSES").toList

      citizensId should have length 2
    }
  }
}

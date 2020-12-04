package com.bharatsim.engine.graph.ingestion

import com.bharatsim.engine.basicConversions.encoders.DefaultEncoders._
import com.bharatsim.engine.testModels.TestCitizen
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec

class GraphDataTest extends AnyWordSpec with Matchers {
  "addNode" when {
    "provided node" should {
      "store the CsvNode for the provided node" in {
        val graphData = new GraphData()
        val testNode = TestCitizen(45)
        graphData.addNode(1, testNode)

        graphData._nodes.size shouldBe 1
        val csvNode = graphData._nodes.head
        csvNode.uniqueRef shouldBe 1
        csvNode.label shouldBe "TestCitizen"
        csvNode.params shouldBe Map("age" -> 45)
      }
    }

    "provided CsvNode" should {
      "store the CsvNode" in {
        val graphData = new GraphData()
        val csvNode = CsvNode("SomeLabel", 1, Map("key" -> "val"))
        graphData.addNode(csvNode)

        graphData._nodes.size shouldBe 1
        val storedNode = graphData._nodes.head
        storedNode shouldBe csvNode
      }
    }
  }

  "addRelations" when {
    "provided single relation" should {
      "store the same" in {
        val graphData = new GraphData()
        val relation = Relation("StartNode", 1, "REL", "EndNode", 2)
        graphData.addRelations(relation)

        graphData._relations.size shouldBe 1
        val retrievedRelation = graphData._relations.head
        retrievedRelation shouldBe relation
      }
    }

    "provided multiple relations" should {
      "store all" in {
        val graphData = new GraphData()
        val relation1 = Relation("StartNode", 1, "REL", "EndNode", 2)
        val relation2 = Relation("StartNode", 2, "REL", "EndNode", 2)
        graphData.addRelations(relation1, relation2)

        graphData._relations.size shouldBe 2
        val retrievedRelations = graphData._relations
        retrievedRelations should contain theSameElementsAs List(relation1, relation2)
      }
    }
  }
}

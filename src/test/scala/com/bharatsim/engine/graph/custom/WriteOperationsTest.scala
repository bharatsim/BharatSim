package com.bharatsim.engine.graph.custom

import com.bharatsim.engine.graph.GraphProvider.NodeId
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec

import scala.collection.concurrent.TrieMap
import scala.collection.parallel.CollectionConverters.IterableIsParallelizable

class WriteOperationsTest extends AnyWordSpec with Matchers {

  private val repeats = 10

  "update node" when {
    "use with concurrency " should {
      "update all the properties of the node" in {
        for (i <- 1 to repeats) {
          def emptyNode = () => new TrieMap[NodeId, InternalNode]()

          val buffer = Buffer(TrieMap.empty, TrieMap.empty)
          val idGenerator = new IdGenerator
          val writeOperations = new WriteOperations(buffer, emptyNode, idGenerator)

          val label = "label1"
          val propertyName1 = "prop1"
          val propertyName2 = "prop2"
          val propertyName3 = "prop3"
          val propertyName4 = "prop4"
          val nodeId = writeOperations.createNode(label, Map(propertyName1 -> "SomeValue", propertyName2 -> "SomeValue"))

          val propList = List((propertyName1, "Value_1"), (propertyName2, "Value_2"), (propertyName3, "Value_3"), (propertyName4, "Value_4"))
          propList.par.foreach(prop => {
            writeOperations.updateNode(nodeId, Map(prop._1 -> prop._2))
          })

          val actualInIndex = buffer.indexedNodes(nodeId).params.toList
          val actualInNodes = buffer.nodes(label)(nodeId).params.toList

          actualInIndex should contain theSameElementsAs propList
          actualInNodes should contain theSameElementsAs propList

        }
      }
    }
  }


  "create node" when {
    "use with concurrency" should {
      "create node all the node" in {
        for (i <- 1 to repeats) {
          def emptyNode = () => new TrieMap[NodeId, InternalNode]()

          val buffer = Buffer(TrieMap.empty, TrieMap.empty)
          val idGenerator = new IdGenerator
          val writeOperations = new WriteOperations(buffer, emptyNode, idGenerator)

          val node1 = ("label1", Map("prop" -> "value1"))
          val node2 = ("label1", Map("prop" -> "value2"))
          val node3 = ("label2", Map("prop" -> "value3"))
          val node4 = ("label2", Map("prop" -> "value4"))
          val nodes = List(node1, node2, node3, node4)

          nodes.par.foreach(node => {
            writeOperations.createNode(node._1, node._2)
          })

          val actualInIndex = buffer.indexedNodes.toList.map((node) => (node._2.label, node._2.params))
          val actualInNodes = buffer.nodes.values.flatMap((nodes) => nodes.toList.map(node => (node._2.label, node._2.params)))

          actualInIndex should contain theSameElementsAs nodes
          actualInNodes should contain theSameElementsAs nodes

        }
      }
    }
  }

  "create relations ship" when {
    "use with concurrency" should {
      "create all the relationship" in {
        for (i <- 1 to repeats) {

          def emptyNode = () => new TrieMap[NodeId, InternalNode]()

          val buffer = Buffer(TrieMap.empty, TrieMap.empty)
          val idGenerator = new IdGenerator
          val writeOperations = new WriteOperations(buffer, emptyNode, idGenerator)
          val relationLabel = "IN_RELATION"
          val node1 = ("label1", Map("prop" -> "value1"))
          val node2 = ("label1", Map("prop" -> "value2"))
          val node3 = ("label2", Map("prop" -> "value3"))
          val node4 = ("label2", Map("prop" -> "value4"))

          val nodeIds = List(node1, node2, node3, node4).map(node => writeOperations.createNode(node._1, node._2))

          val relation1 = (nodeIds(0), nodeIds(2))
          val relation2 = (nodeIds(0), nodeIds(3))
          val relation3 = (nodeIds(1), nodeIds(2))
          val relation4 = (nodeIds(1), nodeIds(3))

          val expectedNodeRelation = Set(nodeIds(2), nodeIds(3))
          val relationships = List(relation1, relation2, relation3, relation4)
          relationships.par.foreach(relation => {
            writeOperations.createRelationship(relation._1, relationLabel, relation._2)
          })

          val node1RelationsInIndex = buffer.indexedNodes(nodeIds(0)).fetchNeighborsWithLabel(relationLabel)
          val node1RelationsInNode = buffer.nodes(node1._1)(nodeIds(0)).fetchNeighborsWithLabel(relationLabel)
          val node2RelationsInIndex = buffer.indexedNodes(nodeIds(1)).fetchNeighborsWithLabel(relationLabel)
          val node2RelationsInNode = buffer.nodes(node2._1)(nodeIds(1)).fetchNeighborsWithLabel(relationLabel)

          node1RelationsInIndex should contain theSameElementsAs expectedNodeRelation
          node1RelationsInNode should contain theSameElementsAs expectedNodeRelation
          node2RelationsInIndex should contain theSameElementsAs expectedNodeRelation
          node2RelationsInNode should contain theSameElementsAs expectedNodeRelation

        }
      }
    }
  }

}
package com.bharatsim.engine.graph.ingestion

import com.bharatsim.engine.basicConversions.decoders.DefaultDecoders._
import com.bharatsim.engine.basicConversions.encoders.DefaultEncoders._
import com.bharatsim.engine.testModels.StatefulPerson
import com.bharatsim.engine.testModels.TestFSM.IdleState
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec

class NodeExpanderTest extends AnyWordSpec with Matchers {

  "expand" when {
    "provided a StatefulAgent" should {
      "return State node" in {
        val nodeExpander = new NodeExpander()
        val initialState = IdleState(0)
        val statefulAgent = StatefulPerson("test", 34)
        statefulAgent.setInitialState(initialState)

        val data = nodeExpander.expand(1, statefulAgent)
        data._nodes.size shouldBe 1
        val csvNode = data._nodes.head
        csvNode.label shouldBe "IdleState"
        csvNode.params shouldBe Map("idleFor" -> 0)
      }

      "return a relationship between a StatefulAgent and State" in {
        val nodeExpander = new NodeExpander()
        val initialState = IdleState(0)
        val statefulAgent = StatefulPerson("test", 34)
        statefulAgent.setInitialState(initialState)

        val data = nodeExpander.expand(1, statefulAgent)
        data._relations.size shouldBe 1
        val relation = data._relations.head
        relation.relation shouldBe "FSM_STATE"
        relation.fromLabel shouldBe "StatefulPerson"
        relation.fromRef shouldBe 1
        relation.toLabel shouldBe "IdleState"
        relation.toRef shouldBe data._nodes.head.uniqueRef
      }
    }
  }
}

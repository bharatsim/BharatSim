package com.bharatsim.engine.graph.ingestion

import com.bharatsim.engine.fsm.State
import com.bharatsim.engine.models.{Node, StatefulAgent}
import com.bharatsim.engine.utils.Utils.fetchClassName

import scala.reflect.ClassTag

class NodeExpander {
  def expand[T <: Node: ClassTag](nodeRef: Int, node: T): GraphData = {
    node match {
      case statefulAgent: StatefulAgent =>
        val label = fetchClassName[T]

        val initialState = statefulAgent.maybeInitialState.get
        val graphData = GraphData()
        val stateLabel = initialState.label
        val stateRef = State.idGenerator.generateId
        val csvNode = CsvNode(stateLabel, stateRef, initialState.serialize)
        graphData.addNode(csvNode)

        val relation = Relation(label, nodeRef, StatefulAgent.STATE_RELATIONSHIP, stateLabel, stateRef)
        graphData.addRelations(relation)
        graphData
      case _ => GraphData()
    }
  }
}
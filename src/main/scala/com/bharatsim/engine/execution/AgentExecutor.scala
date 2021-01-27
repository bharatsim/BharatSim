package com.bharatsim.engine.execution

import com.bharatsim.engine.execution.NodeWithDecoder.GenericNodeWithDecoder
import com.bharatsim.engine.execution.control.{BehaviourControl, StateControl}
import com.bharatsim.engine.models.StatefulAgent

class AgentExecutor(behaviourControl: BehaviourControl, stateControl: StateControl) {
  def execute(nodeWithDecoder: GenericNodeWithDecoder): Unit = {
    val agent = nodeWithDecoder.toAgent
    behaviourControl.executeFor(agent)

    agent match {
      case statefulAgent: StatefulAgent => stateControl.executeFor(statefulAgent)
      case _ =>
    }
  }
}

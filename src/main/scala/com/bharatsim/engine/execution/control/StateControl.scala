package com.bharatsim.engine.execution.control

import com.bharatsim.engine.Context
import com.bharatsim.engine.models.StatefulAgent

class StateControl(context: Context) {
  def executeFor(statefulAgent: StatefulAgent): Unit = {
    val currentState = statefulAgent.activeState
    currentState.perTickAction(context, statefulAgent)

    val maybeTransition = currentState.transitions.find(_.when(context, statefulAgent))
    if (maybeTransition.isDefined) {
      val transition = maybeTransition.get
      val state = transition.state(context)

      context.graphProvider.deleteNode(currentState.internalId)
      val nodeId = context.graphProvider.createNode(transition.label, transition.serializedState(state))
      context.graphProvider.createRelationship(statefulAgent.internalId, StatefulAgent.STATE_RELATIONSHIP, nodeId)

      statefulAgent.forceUpdateActiveState()
      state.setId(nodeId)
      state.enterAction(context, statefulAgent)
    }
  }
}

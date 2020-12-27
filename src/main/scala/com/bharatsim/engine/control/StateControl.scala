package com.bharatsim.engine.control

import com.bharatsim.engine.Context
import com.bharatsim.engine.models.StatefulAgent

class StateControl(context: Context) {
  def executeFor(statefulAgent: StatefulAgent): Unit = {
    val currentState = statefulAgent.activeState

    val maybeTransition = currentState.transitions.find(_.when(context, statefulAgent))
    if (maybeTransition.isDefined) {
      val transition = maybeTransition.get
      val state = transition.state(context)

      context.graphProvider.deleteNode(currentState.internalId)
      val nodeId = context.graphProvider.createNode(transition.label, transition.serializedState(state))
      context.graphProvider.createRelationship(statefulAgent.internalId, StatefulAgent.STATE_RELATIONSHIP, nodeId)

      statefulAgent.forceUpdateActiveState()
      val updatedState = statefulAgent.activeState

      updatedState.enterAction(context, statefulAgent)
      updatedState.perTickAction(context, statefulAgent)
    } else {
      currentState.perTickAction(context, statefulAgent)
    }
  }
}

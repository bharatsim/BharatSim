package com.bharatsim.engine.execution.tick

import com.bharatsim.engine.Context
import com.bharatsim.engine.listeners.SimulationListenerRegistry

class PostTickActions(context: Context) {
  def execute(): Unit = {
    SimulationListenerRegistry.notifyStepEnd(context)

    invokeActions()
  }

  private def invokeActions(): Unit = {
    context.actions.foreach(conditionalAction => {
      if (conditionalAction.condition(context)) {
        conditionalAction.action.perform(context)
      }
    })
  }
}

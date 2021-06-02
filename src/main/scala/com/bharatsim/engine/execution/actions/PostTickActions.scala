package com.bharatsim.engine.execution.actions

import com.bharatsim.engine.Context
import com.bharatsim.engine.listeners.SimulationListenerRegistry
import com.typesafe.scalalogging.LazyLogging

class PostTickActions(context: Context) extends LazyLogging {
  def execute(): Unit = {
    SimulationListenerRegistry.notifyStepEnd(context)

    invokeActions()
    logger.debug("Done PostTickAction for tick {}", context.getCurrentStep)
  }

  private def invokeActions(): Unit = {
    context.actions.foreach(conditionalAction => {
      if (conditionalAction.condition(context)) {
        conditionalAction.action.perform(context)
      }
    })
  }
}

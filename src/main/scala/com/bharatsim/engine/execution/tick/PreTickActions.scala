package com.bharatsim.engine.execution.tick

import com.bharatsim.engine.Context
import com.bharatsim.engine.listeners.SimulationListenerRegistry
import com.typesafe.scalalogging.LazyLogging

class PreTickActions(context: Context) extends LazyLogging {
  def execute(tick: Int): Unit = {
    logger.info("Tick {}", tick)
    context.setCurrentStep(tick)
    SimulationListenerRegistry.notifyStepStart(context)

    invokeInterventionActions()

    context.perTickCache.clear()
    logger.info("done PreTickAction for tick {}", tick)
  }

  private def invokeInterventionActions(): Unit = {
    context.interventions.inactive
      .foreach(i => {
        if (i.shouldActivate(context)) {
          i.firstTimeAction(context)
          context.interventions.markActive(i)
        }
      })

    context.interventions.active
      .foreach(i => if (i.shouldDeactivate(context)) context.interventions.markInactive(i))

    context.interventions.active.foreach(i => i.whenActiveAction(context))
  }
}

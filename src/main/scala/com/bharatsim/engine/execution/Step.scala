package com.bharatsim.engine.execution

import com.bharatsim.engine.Context
import com.bharatsim.engine.execution.control.{BehaviourControl, StateControl}
import com.bharatsim.engine.listeners.SimulationListenerRegistry
import com.bharatsim.engine.models.{Agent, StatefulAgent}
import com.typesafe.scalalogging.LazyLogging

class Step(stepNumber: Int, context: Context, behaviourControl: BehaviourControl, stateControl: StateControl)
  extends LazyLogging {
  def preStepActions(): Unit = {
    logger.info("Tick {}", stepNumber)
    context.setCurrentStep(stepNumber)
    SimulationListenerRegistry.notifyStepStart(context)

    invokeInterventionActions()

    context.perTickCache.clear()
  }

  def exec(): Unit = {
    context.fetchAgentTypes
      .flatMap(agentType => agentType(context.graphProvider))
      .foreach((agent: Agent) => {
        behaviourControl.executeFor(agent)

        agent match {
          case statefulAgent: StatefulAgent => stateControl.executeFor(statefulAgent)
          case _ =>
        }
      })
  }

  def postStepActions(): Unit = {
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

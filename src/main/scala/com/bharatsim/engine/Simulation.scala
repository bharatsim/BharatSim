package com.bharatsim.engine

import com.bharatsim.engine.control.{BehaviourControl, StateControl}
import com.bharatsim.engine.listeners.SimulationListenerRegistry
import com.bharatsim.engine.models.{Agent, StatefulAgent}
import com.typesafe.scalalogging.LazyLogging

import scala.util.control.Breaks.{break, breakable}

class Simulation(context: Context, behaviourControl: BehaviourControl, stateControl: StateControl) extends LazyLogging {
  def run(): Unit = {

    SimulationListenerRegistry.notifySimulationStart(context)

    breakable {
      try {

        executeStateEnterActions()

        for (step <- 1 to context.simulationConfig.simulationSteps) {
          logger.info("Tick {}", step)
          context.setCurrentStep(step)
          SimulationListenerRegistry.notifyStepStart(context)

          invokeActions()

          if (context.stopSimulation) {
            break
          }

          invokeInterventionActions()

          context.perTickCache.clear()

          val agentTypes = context.fetchAgentTypes
          agentTypes.foreach(agentType => {
            agentType(context.graphProvider).foreach((agent: Agent) => {
              behaviourControl.executeFor(agent)

              agent match {
                case statefulAgent: StatefulAgent => stateControl.executeFor(statefulAgent)
                case _ =>
              }
            })
          })

          SimulationListenerRegistry.notifyStepEnd(context)
        }
      } finally {
        SimulationListenerRegistry.notifySimulationEnd(context)
      }
    }
  }

  private def executeStateEnterActions():Unit = {
    val agentTypes = context.fetchAgentTypes
    agentTypes.foreach(agentType => {
      agentType(context.graphProvider).foreach((agent: Agent) => {
        agent match {
          case statefulAgent: StatefulAgent =>
            statefulAgent.activeState.enterAction(context, statefulAgent)
          case _ =>
        }
      })
    })
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

object Simulation {
  def run()(implicit context: Context): Unit = {
    val behaviourControl = new BehaviourControl(context)
    val stateControl = new StateControl(context)
    val simulation = new Simulation(context, behaviourControl, stateControl)

    simulation.run()
  }
}

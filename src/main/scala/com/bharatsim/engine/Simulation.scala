package com.bharatsim.engine

import com.bharatsim.engine.listeners.SimulationListenerRegistry
import com.bharatsim.engine.models.{Agent, StatefulAgent}
import com.typesafe.scalalogging.LazyLogging

import scala.util.control.Breaks.{break, breakable}

class Simulation(context: Context) extends LazyLogging {
  def run(): Unit = {

    SimulationListenerRegistry.notifySimulationStart(context)

    breakable {
      for (step <- 1 to context.simulationConfig.simulationSteps) {
        logger.info("Tick {}", step)
        context.setCurrentStep(step)
        SimulationListenerRegistry.notifyStepStart(context)

        invokeActions()

        if (context.stopSimulation) {
          break
        }

        invokeInterventionActions()

        val agentTypes = context.fetchAgentTypes
        agentTypes.foreach(agentType => {
          agentType(context.graphProvider).foreach((agent: Agent) => {
            agent.behaviours.foreach(b => b(context))
            agent match {
              case statefulAgent: StatefulAgent =>
                handleStateControl(statefulAgent)
              case _ =>
            }
          })
        })

        SimulationListenerRegistry.notifyStepEnd(context)
      }
    }

    SimulationListenerRegistry.notifySimulationEnd(context)
  }

  private def handleStateControl(statefulAgent: StatefulAgent): Unit = {
    if (statefulAgent.hasInitialState) {
      val currentState = statefulAgent.activeState

      val maybeTransition = currentState.transitions.find(_.when(context, statefulAgent))
      if(maybeTransition.isDefined) {
        val transition = maybeTransition.get
        val state = transition.state(context)

        context.graphProvider.deleteNode(currentState.internalId)
        val nodeId = context.graphProvider.createNode(transition.label, transition.serializedState(state))
        context.graphProvider.createRelationship(statefulAgent.internalId, StatefulAgent.STATE_RELATIONSHIP, nodeId)

        state.enterAction(context, statefulAgent)
        state.perTickAction(context, statefulAgent)
      } else {
        currentState.perTickAction(context, statefulAgent)
      }
    } else {
      throw new RuntimeException("Stateful agent does not have initial state")
    }
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
    val simulation = new Simulation(context)
    simulation.run()
  }
}

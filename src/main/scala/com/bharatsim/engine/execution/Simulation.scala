package com.bharatsim.engine.execution

import com.bharatsim.engine.Context
import com.bharatsim.engine.execution.control.{BehaviourControl, StateControl}
import com.bharatsim.engine.listeners.SimulationListenerRegistry
import com.bharatsim.engine.models.{Agent, StatefulAgent}
import com.typesafe.scalalogging.LazyLogging

import scala.annotation.tailrec

class Simulation(context: Context, behaviourControl: BehaviourControl, stateControl: StateControl) extends LazyLogging {
  def invokePreSimulationActions(): Unit = {
    SimulationListenerRegistry.notifySimulationStart(context)
    executeStateEnterActions()
  }

  def run(): Unit = {
    val maxSteps = context.simulationConfig.simulationSteps

    @tailrec
    def loop(currentStep: Int): Unit = {
      val endOfSimulation = currentStep > maxSteps || context.stopSimulation

      if(!endOfSimulation) {
        val step = new Step(currentStep, context, behaviourControl, stateControl)
        step.preStepActions()
        step.exec()
        step.postStepActions()

        loop(currentStep + 1)
      }
    }

    loop(1)
  }

  def invokePostSimulationActions(): Unit = {
    SimulationListenerRegistry.notifySimulationEnd(context)
  }

  private def executeStateEnterActions(): Unit = {
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
}

object Simulation {
  def run()(implicit context: Context): Unit = {
    val behaviourControl = new BehaviourControl(context)
    val stateControl = new StateControl(context)
    val simulation = new Simulation(context, behaviourControl, stateControl)

    try {
      simulation.invokePreSimulationActions()
      simulation.run()
    } finally {
      simulation.invokePostSimulationActions()
    }
  }
}

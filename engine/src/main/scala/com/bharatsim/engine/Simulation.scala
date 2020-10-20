package com.bharatsim.engine

import com.bharatsim.engine.listners.SimulationListenerRegistry
import com.bharatsim.engine.models.Agent
import com.typesafe.scalalogging.LazyLogging

class Simulation(context: Context) extends LazyLogging {
  def run(): Unit = {

    SimulationListenerRegistry.notifySimulationStart(context)
    for (step <- 1 to context.simulationConfig.simulationSteps) {
      logger.info("Tick {}", step)
      context.setCurrentStep(step)
      SimulationListenerRegistry.notifyStepStart(context)

      val agentTypes = context.fetchAgentTypes

      agentTypes.foreach(agentType => {
        agentType(context.graphProvider).foreach((agent: Agent) => {
          agent.behaviours.foreach(b => b(context))
        })
      })

      SimulationListenerRegistry.notifyStepEnd(context)
    }
    SimulationListenerRegistry.notifySimulationEnd(context)
  }
}

object Simulation {
  def run()(implicit context: Context): Unit = {
    val simulation = new Simulation(context)
    simulation.run()
  }
}

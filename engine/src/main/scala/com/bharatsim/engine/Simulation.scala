package com.bharatsim.engine

object Simulation {

  def run(context: Context): Unit = {
    for (step <- 1 to context.simulationContext.simulationSteps) {
      context.simulationContext.setCurrentStep(step)

      context.agents.getAll.foreach(agent => agent.behaviours.foreach(b => b(context)))
    }
  }
}

package com.bharatsim.engine

object Simulation {

  def run(context: Context): Unit = {
    for (step <- 1 to context.simulationContext.simulationSteps) {
      for (agent <- context.agents.getAll) {
        context.simulationContext.setCurrentStep(step)
        agent.behaviours.foreach(b => b(context))
      }
    }
  }
}

package com.bharatsim.engine

import com.bharatsim.model.Citizen

object Simulation {

  def run(context: Context): Unit = {
    for (step <- 1 to context.simulationContext.simulationSteps) {
      context.simulationContext.setCurrentStep(step)

      context.graphProvider.fetchNodes("Citizen").foreach(agent => {
        val x = agent.asInstanceOf[Agent]
        x.behaviours.foreach(b => b(context))
      })
    }
  }
}

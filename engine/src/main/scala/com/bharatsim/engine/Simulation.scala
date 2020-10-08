package com.bharatsim.engine

object Simulation {

  def run(context: Context): Unit = {
    for (step <- 1 to context.simulationContext.simulationSteps) {
      context.simulationContext.setCurrentStep(step)

      val agentTypes = context.fetchAgentTypes

      agentTypes.foreach(agentType => {
        agentType(context.graphProvider).foreach((agent: Agent) => {
          agent.behaviours.foreach(b => b(context))
        })
      })
    }
  }
}

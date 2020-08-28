package com.bharatsim.engine

object Simulation {

  def run(context: Context): Unit = {

    val maxBehaviorCount =
      context.agents.getAll().map(_.behaviours.size).maxOption.getOrElse(0)
//    for (step <- 1 to context.simulationContext.simulationSteps) {
    //      context.simulationContext.setCurrentStep(step);
    //      for (behaviourIndex <- 0 until maxBehaviorCount) {
    //        for (agent <- context.agents.getAll()
    //             if agent.behaviours.isDefinedAt(behaviourIndex)) {
    //          agent.behaviours(behaviourIndex)(context);
    //        }
    //      }
    //    }

    for (step <- 1 to context.simulationContext.simulationSteps) {
      for (agent <- context.agents.getAll()) {
        context.simulationContext.setCurrentStep(step);
        for (behaviourIndex <- 0 until maxBehaviorCount) {
          if (agent.behaviours.isDefinedAt(behaviourIndex)) {
            agent.behaviours(behaviourIndex)(context);
          }
        }
      }
    }

  }

}

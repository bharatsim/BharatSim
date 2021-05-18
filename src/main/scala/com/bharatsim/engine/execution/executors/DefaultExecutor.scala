package com.bharatsim.engine.execution.executors

import com.bharatsim.engine.execution.{SimulationDefinition, SimulationExecutor, Tick}
import com.bharatsim.engine.graph.GraphProviderFactory
import com.bharatsim.engine.{ApplicationConfigFactory, CollectionBased, Context}

import scala.annotation.tailrec

class DefaultExecutor extends SimulationExecutor {

  def runSimulation(context: Context) = {
    val maxSteps = context.simulationConfig.simulationSteps
    val executionMode = ApplicationConfigFactory.config.executionMode
    val executorContext = new ExecutorContext(context)
    val actions = executorContext.actions
    @tailrec
    def loop(currentStep: Int): Unit = {
      val endOfSimulation = currentStep > maxSteps || context.stopSimulation

      if (!endOfSimulation) {
        val tick = new Tick(
          currentStep,
          context,
          actions.preTick,
          executorContext.agentExecutor,
          actions.postTick
        )
        tick.preStepActions()

        if (executionMode == CollectionBased) tick.execParallel()
        else tick.exec()

        tick.postStepActions()

        loop(currentStep + 1)
      }
    }

    try {
      actions.preSimulation.execute()
      loop(1)
    } finally {
      actions.postSimulation.execute()
    }
  }

  override def execute(simulationDefinition: SimulationDefinition): Unit = {
    GraphProviderFactory.init()
    val context = Context()
    simulationDefinition.ingestionStep(context)
    simulationDefinition.simulationBody(context)
    runSimulation(context)
    simulationDefinition.onComplete(context)
  }
}

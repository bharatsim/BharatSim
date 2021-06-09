package com.bharatsim.engine.execution.executors

import com.bharatsim.engine.execution.{SimulationDefinition, Tick}
import com.bharatsim.engine.graph.GraphProviderFactory
import com.bharatsim.engine.{ApplicationConfigFactory, CollectionBased, Context}

import scala.annotation.tailrec

class DefaultExecutor(executorContext: ExecutorContext = new ExecutorContext(), context: Context = Context())
    extends SimulationExecutor {

  def runSimulation(context: Context) = {
    val maxSteps = context.simulationConfig.simulationSteps
    val executionMode = context.simulationConfig.executionMode
    val (agentExecutor, actions) = executorContext.prepare(context)
    @tailrec
    def loop(currentStep: Int): Unit = {
      val endOfSimulation = currentStep > maxSteps || context.stopSimulation

      if (!endOfSimulation) {
        val tick = new Tick(
          currentStep,
          context,
          actions.preTick,
          agentExecutor,
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
    context.graphProvider.clearData()
    simulationDefinition.ingestionStep(context)
    simulationDefinition.simulationBody(context)
    runSimulation(context)
    simulationDefinition.onComplete(context)
  }
}

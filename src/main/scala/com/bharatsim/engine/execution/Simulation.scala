package com.bharatsim.engine.execution

import com.bharatsim.engine._
import com.bharatsim.engine.execution.actorbased.ActorBackedSimulation
import com.bharatsim.engine.execution.control.{BehaviourControl, StateControl}
import com.bharatsim.engine.execution.simulation.{PostSimulationActions, PreSimulationActions}
import com.bharatsim.engine.execution.tick.{PostTickActions, PreTickActions}
import com.bharatsim.engine.graph.GraphProviderFactory
import com.typesafe.scalalogging.LazyLogging

import scala.annotation.tailrec
import scala.concurrent.Await
import scala.concurrent.duration.Duration

class Simulation(
    context: Context,
    applicationConfig: ApplicationConfig,
    agentExecutor: AgentExecutor,
    preSimulationActions: PreSimulationActions,
    postSimulationActions: PostSimulationActions
) extends LazyLogging {
  def invokePreSimulationActions(): Unit = {
    preSimulationActions.execute()
  }

  def run(): Unit = {
    val maxSteps = context.simulationConfig.simulationSteps
    val preTickActions = new PreTickActions(context)
    val postTickActions = new PostTickActions(context)

    @tailrec
    def loop(currentStep: Int): Unit = {
      val endOfSimulation = currentStep > maxSteps || context.stopSimulation

      if (!endOfSimulation) {
        val tick = new Tick(currentStep, context, preTickActions, agentExecutor, postTickActions)
        tick.preStepActions()

        if (applicationConfig.executionMode == CollectionBased) tick.execParallel()
        else tick.exec()

        tick.postStepActions()

        loop(currentStep + 1)
      }
    }

    loop(1)
  }

  def invokePostSimulationActions(): Unit = {
    postSimulationActions.execute()
  }
}

object Simulation {
  private val applicationConfig: ApplicationConfig = ApplicationConfigFactory.config

  def init(): Context = {
    GraphProviderFactory.init()
    Context()
  }

  def run()(implicit context: Context): Unit = {
    val behaviourControl = new BehaviourControl(context)
    val stateControl = new StateControl(context)
    val agentExecutor = new AgentExecutor(behaviourControl, stateControl)
    val preSimulationActions = new PreSimulationActions(context)
    val postSimulationActions = new PostSimulationActions(context)

    if (applicationConfig.executionMode == ActorBased) {
      val eventualDone =
        new ActorBackedSimulation(
          applicationConfig
        ).run(context)
      Await.ready(eventualDone, Duration.Inf)
    } else {
      val simulation =
        new Simulation(context, applicationConfig, agentExecutor, preSimulationActions, postSimulationActions)

      try {
        simulation.invokePreSimulationActions()
        simulation.run()
      } finally {
        simulation.invokePostSimulationActions()
      }
    }
  }
}

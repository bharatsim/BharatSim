package com.bharatsim.engine.execution

import com.bharatsim.engine.Context
import com.bharatsim.engine.execution.executors.ExecutorFactory
import com.typesafe.scalalogging.LazyLogging

private[engine] class DistributedSimulation(executorFactory: ExecutorFactory) extends LazyLogging {
  private var ingestionStep: Context => Unit = _ => {}
  private var simulationBody: Context => Unit = _ => {}
  private var onComplete: Context => Unit = _ => {}
  def ingestData(f: (Context) => Unit): Unit = {
    ingestionStep = f
  }

  def defineSimulation(f: Context => Unit): Unit = {
    simulationBody = f
  }

  def onCompleteSimulation(f: Context => Unit): Unit = {
    onComplete = f
  }

  def run(): Unit = {
    val simulationDef = SimulationDefinition(ingestionStep, simulationBody, onComplete)
    executorFactory.getExecutor().execute(simulationDef)
  }

}

object DistributedSimulation {
  def apply(): DistributedSimulation = new DistributedSimulation(new ExecutorFactory())
}

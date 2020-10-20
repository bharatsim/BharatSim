package com.bharatsim.engine

import com.bharatsim.engine.graph.{GraphProvider, GraphProviderFactory}
import com.bharatsim.engine.models.Agent

import scala.collection.mutable
import scala.collection.mutable.ListBuffer

class Context(val graphProvider: GraphProvider, val dynamics: Dynamics, val simulationConfig: SimulationConfig) {
  private[engine] val schedules = new Schedules
  private[engine] val agentTypes: mutable.ListBuffer[GraphProvider => Iterable[Agent]] = ListBuffer.empty
  private[engine] var currentStep = 0

  def fetchScheduleFor(agent: Agent): Option[Schedule] = {
    schedules.get(agent, this)
  }

  def getCurrentStep: Int = currentStep

  private[engine] def setCurrentStep(step: Int): Unit = {
    currentStep = step
  }

  private[engine] def fetchAgentTypes: ListBuffer[GraphProvider => Iterable[Agent]] = agentTypes
}

object Context {
  def apply(dynamics: Dynamics, simulationConfig: SimulationConfig): Context =
    new Context(GraphProviderFactory.get, dynamics, simulationConfig)
}

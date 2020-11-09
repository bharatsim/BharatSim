package com.bharatsim.engine

import com.bharatsim.engine.actions.ConditionalAction
import com.bharatsim.engine.graph.{GraphProvider, GraphProviderFactory}
import com.bharatsim.engine.intervention.{Intervention, Interventions}
import com.bharatsim.engine.models.Agent

import scala.collection.mutable
import scala.collection.mutable.ListBuffer

class Context(val graphProvider: GraphProvider, val dynamics: Dynamics, val simulationConfig: SimulationConfig) {
  private[engine] val schedules = new Schedules
  private[engine] val agentTypes: mutable.ListBuffer[GraphProvider => Iterable[Agent]] = ListBuffer.empty
  private[engine] var currentStep = 0
  private[engine] val actions: ListBuffer[ConditionalAction] = ListBuffer.empty
  private[engine] var stopSimulation = false
  private[engine] val interventions = new Interventions()

  def fetchScheduleFor(agent: Agent): Option[Schedule] = {
    schedules.get(agent, this)
  }

  def getCurrentStep: Int = currentStep

  private[engine] def setCurrentStep(step: Int): Unit = {
    currentStep = step
  }

  private[engine] def fetchAgentTypes: ListBuffer[GraphProvider => Iterable[Agent]] = agentTypes

  def activeInterventionNames: Set[String] = interventions.activeNames
}

object Context {
  def apply(dynamics: Dynamics, simulationConfig: SimulationConfig): Context =
    new Context(GraphProviderFactory.get, dynamics, simulationConfig)
}

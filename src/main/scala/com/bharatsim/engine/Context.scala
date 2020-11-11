package com.bharatsim.engine

import com.bharatsim.engine.actions.ConditionalAction
import com.bharatsim.engine.graph.{GraphProvider, GraphProviderFactory}
import com.bharatsim.engine.intervention.{Intervention, Interventions}
import com.bharatsim.engine.models.Agent

import scala.collection.mutable
import scala.collection.mutable.ListBuffer

/**
  * Context holds all the configurations and data about current state of simulation.
  * @param graphProvider [Optional] instance of [[graph.GraphProvider GraphProvider]]
  * @param dynamics    instance of [[Dynamics]] for current simulation
  * @param simulationConfig instance of [[simulationConfig]] for the simulation
  */
class Context(val graphProvider: GraphProvider, val dynamics: Dynamics, val simulationConfig: SimulationConfig) {
  private[engine] val schedules = new Schedules
  private[engine] val agentTypes: mutable.ListBuffer[GraphProvider => Iterable[Agent]] = ListBuffer.empty
  private[engine] var currentStep = 0
  private[engine] val actions: ListBuffer[ConditionalAction] = ListBuffer.empty
  private[engine] var stopSimulation = false
  private[engine] val interventions = new Interventions()

  /**
    * Gets a matching schedule from all the registered schedules
    * @param agent instance of [[models.Agent Agent]] for which schedule is to be fetched
    * @return a [[Schedule]] when match is found
    */
  def fetchScheduleFor(agent: Agent): Option[Schedule] = {
    schedules.get(agent, this)
  }

  /**
    * Gets a current step of the simulation
    * @return current step of the simulation
    */
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
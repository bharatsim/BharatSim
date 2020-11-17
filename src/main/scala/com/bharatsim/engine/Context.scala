package com.bharatsim.engine

import com.bharatsim.engine.actions.ConditionalAction
import com.bharatsim.engine.graph.{GraphProvider, GraphProviderFactory}
import com.bharatsim.engine.intervention.{Intervention, Interventions}
import com.bharatsim.engine.models.Agent

import scala.collection.mutable
import scala.collection.mutable.ListBuffer

/**
  * Context holds all the configurations and state of the simulation.
  * @param graphProvider instance of [[graph.GraphProvider GraphProvider]]
  * @param dynamics    instance of [[Dynamics]] for current simulation
  * @param simulationConfig instance of [[SimulationConfig]] for the simulation
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

  /** Retrieves list of active interventions at the current tick
    *
    * @return Set of intervention names that are active at the current tick
    */
  def activeInterventionNames: Set[String] = interventions.activeNames

  private[engine] def setCurrentStep(step: Int): Unit = {
    currentStep = step
  }

  private[engine] def fetchAgentTypes: ListBuffer[GraphProvider => Iterable[Agent]] = agentTypes
}

object Context {

  /** Creator method for Context
    *
    * @param dynamics    instance of [[Dynamics]] for current simulation
    * @param simulationConfig instance of [[SimulationConfig]] for the simulation
    */
  def apply(dynamics: Dynamics, simulationConfig: SimulationConfig): Context =
    new Context(GraphProviderFactory.get, dynamics, simulationConfig)
}

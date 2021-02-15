package com.bharatsim.engine

import com.bharatsim.engine.LabelWithDecoder.GenericLabelWithDecoder
import com.bharatsim.engine.actions.ConditionalAction
import com.bharatsim.engine.cache.PerTickCache
import com.bharatsim.engine.execution.NodeWithDecoder
import com.bharatsim.engine.execution.NodeWithDecoder.GenericNodeWithDecoder
import com.bharatsim.engine.graph.{GraphProvider, GraphProviderFactory}
import com.bharatsim.engine.intervention.Interventions
import com.bharatsim.engine.models.Agent

import scala.collection.concurrent.TrieMap
import scala.collection.mutable
import scala.collection.mutable.ListBuffer

/**
  * Context holds all the configurations and state of the simulation.
  *
  * @param graphProvider    instance of [[graph.GraphProvider GraphProvider]]
  * @param dynamics         instance of [[Dynamics]] for current simulation
  * @param simulationConfig instance of [[SimulationConfig]] for the simulation
  */
class Context(
    val graphProvider: GraphProvider,
    val dynamics: Dynamics,
    val simulationConfig: SimulationConfig,
    val perTickCache: PerTickCache
) {
  private[engine] val schedules = new Schedules
  private[engine] val agentTypes: mutable.ListBuffer[GenericLabelWithDecoder] = ListBuffer.empty
  private[engine] var currentStep = 0
  private[engine] val actions: ListBuffer[ConditionalAction] = ListBuffer.empty
  private[engine] var stopSimulation = false
  private[engine] val interventions = new Interventions()
  private var proxyActiveInterventions: Set[String] = Set.empty

  /**
    * Gets a matching schedule from all the registered schedules
    *
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
  def activeInterventionNames: Set[String] = {
    if (ApplicationConfigFactory.config.hasEngineMainRole()) {
      return proxyActiveInterventions
    }
    interventions.activeNames
  }

  private[engine] def setCurrentStep(step: Int): Unit = {
    currentStep = step
  }

  private[engine] def setActiveIntervention(activeInterventions: Set[String]): Unit = {
    proxyActiveInterventions = activeInterventions
  }

  private[engine] def registeredNodesWithDecoder: Iterable[GenericNodeWithDecoder] =
    agentTypes.flatMap(labelWithDecoder =>
      graphProvider
        .fetchNodes(labelWithDecoder.label)
        .map(node => NodeWithDecoder(node, labelWithDecoder.decoder))
    )

  private[engine] def agentLabels: Iterable[String] = agentTypes.map(_.label)
}

object Context {

  /** Creator method for Context
    *
    * @param dynamics          instance of [[Dynamics]] for current simulation
    * @param simulationConfig  instance of [[SimulationConfig]] for the simulation
    * @param applicationConfig optional instance of [[ApplicationConfig]]
    */
  def apply(
      dynamics: Dynamics,
      simulationConfig: SimulationConfig,
      applicationConfig: ApplicationConfig = ApplicationConfigFactory.config
  ): Context = {
    val perTickCache: PerTickCache = buildCache(applicationConfig)
    new Context(GraphProviderFactory.get, dynamics, simulationConfig, perTickCache)
  }

  private def buildCache(applicationConfig: ApplicationConfig) = {
    applicationConfig.executionMode match {
      case NoParallelism => new PerTickCache()
      case _             => new PerTickCache(TrieMap.empty)
    }
  }
}

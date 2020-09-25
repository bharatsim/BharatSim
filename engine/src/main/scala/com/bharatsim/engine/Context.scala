package com.bharatsim.engine

import com.bharatsim.engine.graph.{GraphProvider, GraphProviderFactory}

import scala.collection.mutable
import scala.collection.mutable.ListBuffer
import scala.reflect.{ClassTag, classTag}

class Context(val graphProvider: GraphProvider, val dynamics: Dynamics, val simulationContext: SimulationContext) {
  private val agentTypes: mutable.ListBuffer[String] = ListBuffer.empty
  val schedules = new Schedules;

  def registerAgent[T: ClassTag]: Unit = agentTypes.addOne(classTag[T].runtimeClass.getName)

  def fetchAgentTypes: ListBuffer[String] = agentTypes

}

object Context {
  def apply(dynamics: Dynamics, simulationContext: SimulationContext): Context =
    new Context(GraphProviderFactory.get, dynamics, simulationContext)
}

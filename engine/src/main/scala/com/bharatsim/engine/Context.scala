package com.bharatsim.engine

import com.bharatsim.engine.basicConversions.decoders.BasicMapDecoder
import com.bharatsim.engine.graph.{GraphProvider, GraphProviderFactory}
import com.bharatsim.engine.models.Agent
import com.bharatsim.engine.utils.Utils

import scala.collection.mutable
import scala.collection.mutable.ListBuffer
import scala.reflect.ClassTag

class Context(val graphProvider: GraphProvider, val dynamics: Dynamics, val simulationContext: SimulationContext) {
  private val schedules = new Schedules
  private val agentTypes: mutable.ListBuffer[GraphProvider => Iterable[Agent]] = ListBuffer.empty

  def registerAgent[T <: Agent : ClassTag](implicit decoder: BasicMapDecoder[T]): Unit = {
    agentTypes.addOne((gp: GraphProvider) => gp.fetchNodes(Utils.fetchClassName[T]).map(_.as[T]))
  }

  def addSchedule(schedule: Schedule, fn: (Agent, Context) => Boolean): Unit = {
    schedules.addSchedule(schedule, fn)
  }

  def registerSchedules(s1: List[(Schedule, (Agent, Context) => Boolean)]): Unit = {
    s1.foreach(sc => schedules.addSchedule(sc._1, sc._2))
  }

  def fetchSchedules: Schedules = schedules

  private[engine] def fetchAgentTypes: ListBuffer[GraphProvider => Iterable[Agent]] = agentTypes

}

object Context {
  def apply(dynamics: Dynamics, simulationContext: SimulationContext): Context =
    new Context(GraphProviderFactory.get, dynamics, simulationContext)
}

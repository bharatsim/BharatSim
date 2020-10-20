package com.bharatsim.engine

import com.bharatsim.engine.actions.{Action, ConditionalAction}
import com.bharatsim.engine.basicConversions.decoders.BasicMapDecoder
import com.bharatsim.engine.graph.{GraphData, GraphProvider}
import com.bharatsim.engine.models.Agent
import com.bharatsim.engine.utils.Utils

import scala.reflect.ClassTag

object ContextBuilder {
  def registerSchedules(s1: (Schedule, (Agent, Context) => Boolean), more: (Schedule, (Agent, Context) => Boolean)*)(implicit context: Context): Unit = {
    val allSchedules = s1 :: more.toList
    allSchedules.foreach(sc => context.schedules.add(sc._1, sc._2))
  }

  def ingestCSVData(path: String, fn: Map[String, String] => GraphData)(implicit context: Context): Unit = {
    context.graphProvider.ingestFromCsv(path, Some(fn))
  }

  def ingestDataUsingCSV(path: String)(implicit context: Context): Unit = {
    context.graphProvider.ingestFromCsv(path, None)
  }

  def teardown()(implicit context: Context): Unit = {
    context.graphProvider.shutdown()
  }

  def registerAgent[T <: Agent : ClassTag](implicit decoder: BasicMapDecoder[T], context: Context): Unit = {
    context.agentTypes.addOne((gp: GraphProvider) => gp.fetchNodes(Utils.fetchClassName[T]).map(_.as[T]))
  }

  def registerAction(action: Action, condition: Context => Boolean)(implicit context: Context): Unit = {
    context.actions.addOne(ConditionalAction(action, condition))
  }
}

package com.bharatsim.engine

import com.bharatsim.engine.actions.{Action, ConditionalAction}
import com.bharatsim.engine.basicConversions.decoders.BasicMapDecoder
import com.bharatsim.engine.basicConversions.encoders.BasicMapEncoder
import com.bharatsim.engine.fsm.State
import com.bharatsim.engine.graph.ingestion.GraphData
import com.bharatsim.engine.intervention.Intervention
import com.bharatsim.engine.models.Agent
import com.bharatsim.engine.utils.Utils

import scala.reflect.ClassTag

/**
  * Helper object to build context for the simulation
  */
object ContextBuilder {

  /**
    * Register the schedule with conditions specifying when schedule is applicable.
    * @param s1  a tuple with a [[Schedule]], function specifying condition and priority
    * @param more additional [[Schedule]], condition and priority tuples.
    * @param context instance of [[Context]] for the simulation
    */
  def registerSchedules(
      s1: (Schedule, (Agent, Context) => Boolean, Int),
      more: (Schedule, (Agent, Context) => Boolean, Int)*
  )(implicit
      context: Context
  ): Unit = {
    val allSchedules = s1 :: more.toList
    allSchedules.foreach(sc => context.schedules.add(sc._1, sc._2, sc._3))
  }

  /**
    * Ingest data from csv
    * @param path a path to CSV file
    * @param fn mapper function
    * @param context instance of [[Context]] for the simulation
    */
  def ingestCSVData(path: String, fn: Map[String, String] => GraphData)(implicit context: Context): Unit = {
    context.graphProvider.ingestFromCsv(path, Some(fn))
  }

  def ingestDataUsingCSV(path: String)(implicit context: Context): Unit = {
    context.graphProvider.ingestFromCsv(path, None)
  }

  /**
    * Clean the data and close the connection with database
    *
    * @param cleanData Defaults to true, can be overridden if data cleaning needs to be avoided
    * @param context   instance of [[Context]] for the simulation
    */
  def teardown(cleanData: Boolean = true)(implicit context: Context): Unit = {
    if (cleanData) context.graphProvider.clearData()
    context.graphProvider.shutdown()
  }

  /**
    * Register An Agent
    *
    * @param decoder is basic decoder for the Agent type T
    *             import com.bharatsim.engine.basicConversions.decoders.DefaultDecoders._
    * @param context instance of [[Context]] for the simulation
    * @tparam T type of the Agent
    */
  def registerAgent[T <: Agent: ClassTag](implicit decoder: BasicMapDecoder[T], context: Context): Unit = {
    val label = Utils.fetchClassName[T]
    context.agentTypes.put(label, decoder)
  }

  def registerState[T <: State: ClassTag](implicit encoder: BasicMapEncoder[T], decoder: BasicMapDecoder[T]): Unit = {
    val className = Utils.fetchClassName[T]
    State.saveSerde(className)
  }

  /**
    * Register a [[actions.ConditionalAction ConditionalAction]]
    * @param action instance of [[actions.Action Action]] to be registered
    * @param condition a condition specifying when to execute action
    * @param context instance of [[Context]] for the simulation
    */
  def registerAction(action: Action, condition: Context => Boolean)(implicit context: Context): Unit = {
    context.actions.addOne(ConditionalAction(action, condition))
  }

  /** Register an intervention with engine
    *
    * @param intervention instance of [[com.bharatsim.engine.intervention.Intervention Intervention]]
    * @param context instance of [[Context]] for the simulation
    */
  def registerIntervention(intervention: Intervention)(implicit context: Context): Unit =
    context.interventions.add(intervention)
}

package com.bharatsim.engine.listners

import com.bharatsim.engine.Context

import scala.collection.mutable.ListBuffer

object SimulationListenerRegistry {
  private val listeners = new ListBuffer[SimulationListener]

  def register(listener: SimulationListener): Unit = {
    listeners.addOne(listener)
  }

  private[engine] def notifySimulationStart(context: Context): Unit = {
    listeners.foreach(_.onSimulationStart(context))
  }
  private[engine] def notifySimulationEnd(context: Context): Unit = {
    listeners.foreach(_.onSimulationEnd(context))
  }
  private[engine] def notifyStepStart(context: Context): Unit = {
    listeners.foreach(_.onStepStart(context))
  }
  private[engine] def notifyStepEnd(context: Context): Unit = {
    listeners.foreach(_.onStepEnd(context))
  }

}

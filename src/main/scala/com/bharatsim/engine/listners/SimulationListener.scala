package com.bharatsim.engine.listners

import com.bharatsim.engine.Context

trait SimulationListener {

  def onSimulationStart(context: Context): Unit

  def onStepStart(context: Context): Unit

  def onStepEnd(context: Context): Unit

  def onSimulationEnd(context: Context): Unit
}

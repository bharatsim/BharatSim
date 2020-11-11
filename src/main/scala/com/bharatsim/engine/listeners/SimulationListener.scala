package com.bharatsim.engine.listeners

import com.bharatsim.engine.Context

/**
  *  Simulation listener provides hooks on simulation progress.
  *  Modeller can extend the trait to do some task as simulation progress.
  */
trait SimulationListener {

  /**
    * Called when simulation is about to start
    * @param context current context of simulation
    */
  def onSimulationStart(context: Context): Unit

  /**
    * Called at start of each step of simulation
    * @param context current context of simulation
    */
  def onStepStart(context: Context): Unit

  /**
    * Called at end of each step of simulation
    * @param context current context of simulation
    */
  def onStepEnd(context: Context): Unit

  /**
    * Called when simulation has reached the end
    * @param context current context of simulation
    */
  def onSimulationEnd(context: Context): Unit
}

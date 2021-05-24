package com.bharatsim.engine.actions

import com.bharatsim.engine.Context
import com.typesafe.scalalogging.LazyLogging

sealed trait Action {
  def perform(context: Context): Unit
}

/**
  * The action stops the simulation execution.
  */
case object StopSimulation extends Action with LazyLogging {
  override def perform(context: Context): Unit = {
    logger.info("Stop action has been triggered")
    context.stopSimulation = true
  }
}

package com.bharatsim.engine.actions

import com.bharatsim.engine.Context

sealed trait Action {
  def perform(context: Context): Unit
}

case object StopSimulation extends Action {
  override def perform(context: Context): Unit = {
    context.stopSimulation = true
  }
}

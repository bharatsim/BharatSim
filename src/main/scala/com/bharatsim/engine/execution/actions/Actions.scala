package com.bharatsim.engine.execution.actions
import com.bharatsim.engine.Context

class Actions(context: Context) {

  val preSimulation = new PreSimulationActions(context)
  val postSimulation = new PostSimulationActions(context)
  val preTick = new PreTickActions(context)
  val postTick = new PostTickActions(context)
}

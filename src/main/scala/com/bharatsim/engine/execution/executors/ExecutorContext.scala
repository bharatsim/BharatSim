package com.bharatsim.engine.execution.executors

import com.bharatsim.engine.Context
import com.bharatsim.engine.execution.AgentExecutor
import com.bharatsim.engine.execution.control.{BehaviourControl, StateControl}
import com.bharatsim.engine.execution.simulation.{PostSimulationActions, PreSimulationActions}
import com.bharatsim.engine.execution.tick.{PostTickActions, PreTickActions}

class ExecutorContext(context: Context) {
  private val behaviourControl = new BehaviourControl(context)
  private val stateControl = new StateControl(context)
  val agentExecutor = new AgentExecutor(behaviourControl, stateControl)
  val preSimulationActions = new PreSimulationActions(context)
  val postSimulationActions = new PostSimulationActions(context)
  val preTickActions = new PreTickActions(context)
  val postTickActions = new PostTickActions(context)
}

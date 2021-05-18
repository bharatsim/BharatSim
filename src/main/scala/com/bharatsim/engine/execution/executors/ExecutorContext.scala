package com.bharatsim.engine.execution.executors

import com.bharatsim.engine.Context
import com.bharatsim.engine.execution.AgentExecutor
import com.bharatsim.engine.execution.actions.{Actions}
import com.bharatsim.engine.execution.control.{BehaviourControl, StateControl}

class ExecutorContext(context: Context) {
  private val behaviourControl = new BehaviourControl(context)
  private val stateControl = new StateControl(context)
  val agentExecutor = new AgentExecutor(behaviourControl, stateControl)
  val actions = new Actions(context)
}

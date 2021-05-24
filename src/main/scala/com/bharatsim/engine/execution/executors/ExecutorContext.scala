package com.bharatsim.engine.execution.executors

import com.bharatsim.engine.Context
import com.bharatsim.engine.execution.AgentExecutor
import com.bharatsim.engine.execution.actions.{Actions}
import com.bharatsim.engine.execution.control.{BehaviourControl, StateControl}

class ExecutorContext {
  def prepare(context: Context): (AgentExecutor, Actions) = {
    val behaviourControl = new BehaviourControl(context)
    val stateControl = new StateControl(context)
    val agentExecutor = new AgentExecutor(behaviourControl, stateControl)
    val actions = new Actions(context)
    (agentExecutor, actions)
  }
}

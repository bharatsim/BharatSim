package com.bharatsim.engine.control

import com.bharatsim.engine.Context
import com.bharatsim.engine.models.Agent

class BehaviourControl(context: Context) {
  def executeFor(agent: Agent): Unit = {
    agent.behaviours.foreach(b => b(context))
  }
}

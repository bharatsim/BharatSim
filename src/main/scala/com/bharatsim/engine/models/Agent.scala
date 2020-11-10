package com.bharatsim.engine.models

import com.bharatsim.engine.Context

import scala.collection.mutable

/**
  * In agent-based modeling (ABM), a system is modeled as a collection of autonomous decision-making entities called agent.
  * An agent can be defined by extending the `Agent` class
  */
class Agent() extends Node {
  private[engine] val behaviours: mutable.ListBuffer[Function[Context, Unit]] = mutable.ListBuffer.empty

  /**     The Behaviours that agent exhibit during simulation must be defined using the `addBehaviour` method
    *      @param behaviour the function that specifies the behaviour
    */
  protected[engine] def addBehaviour(behaviour: Function[Context, Unit]): Unit = {
    behaviours.addOne(behaviour)
  }
}

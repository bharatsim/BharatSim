package com.bharatsim.engine.models

import com.bharatsim.engine.Context

import scala.collection.mutable

class Agent() extends Node {
  private[engine] val behaviours: mutable.ListBuffer[Function[Context, Unit]] = mutable.ListBuffer.empty

  protected[engine] def addBehaviour(behaviour: Function[Context, Unit]): Unit = {
    behaviours.addOne(behaviour)
  }
}

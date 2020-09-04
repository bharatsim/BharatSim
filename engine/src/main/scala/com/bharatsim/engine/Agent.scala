package com.bharatsim.engine

import com.bharatsim.engine.Agent.idGenerator

import scala.collection.mutable

class Agent(identifier: Int = idGenerator.generateId) extends Node {
  override val id: Int = identifier
  private[engine] val behaviours: mutable.ListBuffer[Function[Context, Unit]] = mutable.ListBuffer.empty

  protected[engine] def addBehaviour(behaviour: Function[Context, Unit]): Unit = {
    behaviours.addOne(behaviour)
  }
}

object Agent {
  private val idGenerator = new IdGenerator
}

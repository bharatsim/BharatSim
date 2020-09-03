package com.bharatsim.engine

import com.bharatsim.engine.Agent.idGenerator

import scala.collection.mutable

class Agent(identifier: Int = idGenerator.generateId) extends Identity {
  override val id: Int = identifier
  private[engine] val behaviours: mutable.ListBuffer[Function[Context, Unit]] =
    mutable.ListBuffer.empty
  private var network: Option[Network] = None

  def getNetwork: Option[Network] = network

  private[engine] def setNetwork(newNetwork: Network): Unit = {
    network = Some(newNetwork)
  }

  protected[engine] def addBehaviour(behaviour: Function[Context, Unit]): Unit = {
    behaviours.addOne(behaviour)
  }
}

object Agent {
  private val idGenerator = new IdGenerator
}

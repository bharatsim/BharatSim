package com.bharatsim.engine

import com.bharatsim.engine.Network.idGenerator

import scala.collection.mutable

class Network(identifier: Int = idGenerator.generateId) extends Identity {
  import Network._

  override val id = identifier
  private val members = mutable.HashSet[Agent]();

  def connectAgent(agent: Agent): Unit = {
    val existingNetwork = agent.getNetwork();
    if (existingNetwork.isDefined) {
      existingNetwork.get.disconnectAgent(agent);
    }
    members.add(agent);
    agent.setNetwork(this)
  }

  private def disconnectAgent(agent: Agent): Unit = {
    members.remove(agent);
  }

  def getMembers(): Iterator[Agent] = {
    members.iterator
  }
}

object Network {
  private val idGenerator = new IdGenerator;
}

package com.bharatsim.engine
import com.bharatsim.engine.Node.idGenerator

import scala.collection.mutable

class Node(identifier: Int = idGenerator.generateId) extends Identity {
  override val id: Int = identifier
  private val connections = mutable.HashMap[String, mutable.HashSet[Node]]();

  def unidirectionalConnect(relation: String, to: Node): Unit = {
    val connectedNodes = connections.getOrElseUpdate(relation, new mutable.HashSet[Node]())
    connectedNodes.add(to)
  }

  def bidirectionalConnect(relation: String, to: Node): Unit = {
    val connectedNodes = connections.getOrElseUpdate(relation, new mutable.HashSet[Node]())
    val connectedNodesForTo = to.connections.getOrElseUpdate(relation, new mutable.HashSet[Node]())
    connectedNodes.add(to)
    connectedNodesForTo.add(this);
  }

  def disconnect(relation: String, to: Node): Unit = {
    val connectedNodes = connections.getOrElse(relation, new mutable.HashSet[Node]())
    val connectedNodesForTo = to.connections.getOrElse(relation, new mutable.HashSet[Node]())
    connectedNodes.remove(to);
    connectedNodesForTo.remove(this);
  }

  def getConnections(relation: String): Iterator[Node] = {
    connections.getOrElse(relation, new mutable.HashSet[Node]()).iterator
  }
}

object Node {
  private val idGenerator = new IdGenerator;
}

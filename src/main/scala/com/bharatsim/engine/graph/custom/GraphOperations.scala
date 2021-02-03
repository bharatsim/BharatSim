package com.bharatsim.engine.graph.custom

import com.bharatsim.engine.graph.GraphProvider.NodeId
import com.bharatsim.engine.graph.custom.GraphOperations.{IndexedNodesType, NodesType}

import scala.collection.concurrent.TrieMap
import scala.collection.mutable

case class Buffer(nodes: NodesType, indexedNodes: IndexedNodesType)

trait GraphOperations {
  val readOperations: ReadOperations
  val writeOperations: WriteOperations

  def deleteAll(): Unit = {
    readOperations.clearAll()
    writeOperations.clearAll()
  }

  def emptyNode: () => IndexedNodesType
}

private[engine] class Graph(buffer: Buffer) extends GraphOperations {
  val readOperations = new ReadOperations(buffer)
  val writeOperations = new WriteOperations(buffer, emptyNode)

  override def emptyNode: () => IndexedNodesType = () => new mutable.HashMap[NodeId, InternalNode]()
}

private[engine] class BufferedGraph(val readBuffer: Buffer, val writeBuffer: Buffer) extends GraphOperations {
  val readOperations = new ReadOperations(readBuffer)
  val writeOperations = new WriteOperations(writeBuffer, emptyNode)

  override def emptyNode: () => IndexedNodesType = () => new TrieMap[NodeId, InternalNode]()
}

object GraphOperations {
  type NodesType = mutable.AbstractMap[String, mutable.AbstractMap[NodeId, InternalNode]]
  type IndexedNodesType = mutable.AbstractMap[NodeId, InternalNode]
}

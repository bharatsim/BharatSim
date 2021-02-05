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

private[engine] class Graph(buffer: Buffer, idGenerator: IdGenerator) extends GraphOperations {
  val readOperations = new ReadOperations(buffer)
  val writeOperations = new WriteOperations(buffer, emptyNode, idGenerator)

  override def emptyNode: () => IndexedNodesType = () => new mutable.HashMap[NodeId, InternalNode]()
}

private[engine] class BufferedGraph(readBuffer: Buffer, writeBuffer: Buffer, idGenerator: IdGenerator) extends GraphOperations {
  val readOperations = new ReadOperations(readBuffer)
  val writeOperations = new WriteOperations(writeBuffer, emptyNode, idGenerator)

  override def emptyNode: () => IndexedNodesType = () => new TrieMap[NodeId, InternalNode]()

  def syncBuffers(): BufferedGraph = {
    new BufferedGraph(createSnapshot(writeBuffer), writeBuffer, idGenerator)
  }

  private def createSnapshot(b: Buffer): Buffer = {
    type N = TrieMap[NodeId, InternalNode]
    val newNodes: NodesType = b.nodes.asInstanceOf[TrieMap[String, N]].map(kv => (kv._1, kv._2.snapshot()))
    val newIndexedNodes = b.indexedNodes.asInstanceOf[N].snapshot()
    Buffer(newNodes, newIndexedNodes)
  }
}

object GraphOperations {
  type NodesType = mutable.AbstractMap[String, mutable.AbstractMap[NodeId, InternalNode]]
  type IndexedNodesType = mutable.AbstractMap[NodeId, InternalNode]
}

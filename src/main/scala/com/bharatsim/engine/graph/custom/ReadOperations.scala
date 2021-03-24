package com.bharatsim.engine.graph.custom

import com.bharatsim.engine.graph.GraphNode
import com.bharatsim.engine.graph.GraphProvider.NodeId
import com.bharatsim.engine.graph.patternMatcher.MatchPattern
import com.typesafe.scalalogging.LazyLogging

import scala.collection.mutable

class ReadOperations(buffer: Buffer) extends LazyLogging {
  def clearAll(): Unit = {
    buffer.nodes.clear()
    buffer.indexedNodes.clear()
  }

  def fetchNode(label: String, params: Map[String, Any] = Map.empty): Option[GraphNode] = {
    if (params.isEmpty) {
      if (buffer.nodes.contains(label) && buffer.nodes(label).nonEmpty) {
        Some(buffer.nodes(label).head._2.toGraphNode)
      } else None
    } else {
      if (buffer.nodes.contains(label) && buffer.nodes(label).nonEmpty) {
        val list = filterNodesByMatchingParams(label = label, params = params)
        if (list.nonEmpty) Some(list.head.toGraphNode)
        else None
      } else None
    }
  }

  def fetchNodes(label: String, params: Map[String, Any]): Iterable[GraphNode] = {
    if (params.isEmpty) {
      if (buffer.nodes.contains(label) && buffer.nodes(label).nonEmpty) buffer.nodes(label).values.map(_.toGraphNode)
      else List.empty
    } else {
      if (buffer.nodes.contains(label) && buffer.nodes(label).nonEmpty) {
        filterNodesByMatchingParams(label = label, params = params).map(_.toGraphNode)
      } else List.empty
    }
  }

  def fetchNodes(label: String, matchPattern: MatchPattern): Iterable[GraphNode] = {
    if (buffer.nodes.contains(label) && buffer.nodes(label).nonEmpty) {
      buffer.nodes(label).values.filter(node => matchPattern.eval(node.params)).map(_.toGraphNode)
    } else List.empty
  }

  def fetchCount(label: String, matchPattern: MatchPattern): Int = {
    buffer.nodes
      .getOrElse(label, List.empty)
      .count(nodesForLable => matchPattern.eval(nodesForLable._2.params))
  }

  def fetchNeighborsOf(nodeId: NodeId, allLabels: List[String]): Iterable[GraphNode] = {
    if (buffer.indexedNodes.contains(nodeId)) {
      val node = buffer.indexedNodes(nodeId)
      allLabels
        .map(l => node.fetchNeighborsWithLabel(l))
        .foldLeft(new mutable.HashSet[NodeId]())((acc, mp) => acc ++ mp)
        .map(buffer.indexedNodes(_))
        .map(_.toGraphNode)
    } else {
      logger.debug(s"Node with id $nodeId does not exist")
      Iterable.empty
    }
  }

  def neighborCount(nodeId: NodeId, label: String): Int = {

    if (buffer.indexedNodes.contains(nodeId)) {
      val node = buffer.indexedNodes(nodeId)
      node.fetchNeighborsWithLabelCount(label)
    } else 0
  }

  def neighborCount(nodeId: NodeId, label: String, matchCondition: MatchPattern): Int = {

    if (buffer.indexedNodes.contains(nodeId)) {
      val node = buffer.indexedNodes(nodeId)
      var count = 0
      node
        .fetchNeighborsWithLabel(label)
        .foreach(nodeId => {
          val n = buffer.indexedNodes(nodeId)
          if (matchCondition.eval(n.params)) count += 1
        })
      count
    } else 0
  }

  private def filterNodesByMatchingParams(label: String, params: Map[String, Any]): Iterable[InternalNode] = {
    buffer
      .nodes(label)
      .values
      .filter(node => {
        params
          .map(kv => {
            val value = node.fetchParam(kv._1)
            value.isDefined && value.get == kv._2
          })
          .reduce((a, b) => a && b)
      })
  }
}

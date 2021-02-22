package com.bharatsim.engine.graph.custom

import com.bharatsim.engine.graph.{GraphNode, PartialGraphNode}
import com.bharatsim.engine.graph.GraphProvider.NodeId
import com.bharatsim.engine.graph.patternMatcher.{EmptyPattern, MatchPattern}
import com.typesafe.scalalogging.LazyLogging

import scala.collection.mutable

class ReadOperations(buffer: Buffer) extends LazyLogging {
  val labelledNodes: Map[String, Iterable[InternalNode]] = buffer.nodes.map(kv => (kv._1, kv._2.values)).toMap
  def clearAll(): Unit = {
    buffer.nodes.clear()
    buffer.indexedNodes.clear()
  }

  def fetchNode(label: String, params: Map[String, Any] = Map.empty): Option[GraphNode] = {
    if (params.isEmpty) {
      if (labelledNodes.contains(label) && labelledNodes(label).nonEmpty) {
        Some(labelledNodes(label).head.toGraphNode)
      } else None
    } else {
      if (labelledNodes.contains(label) && labelledNodes(label).nonEmpty) {
        val list = filterNodesByMatchingParams(label = label, params = params)
        if (list.nonEmpty) Some(list.head.toGraphNode)
        else None
      } else None
    }
  }

  def fetchNodes(label: String, params: Map[String, Any]): Iterable[GraphNode] = {
    if (params.isEmpty) {
      if (labelledNodes.contains(label) && labelledNodes(label).nonEmpty) labelledNodes(label).map(_.toGraphNode)
      else List.empty
    } else {
      if (labelledNodes.contains(label) && labelledNodes(label).nonEmpty) {
        filterNodesByMatchingParams(label = label, params = params).map(_.toGraphNode)
      } else List.empty
    }
  }


  def fetchNodes(label: String, params: Map[String, Any], skip: Int, limit: Int): Iterable[GraphNode] = {
    if (params.isEmpty) {
      if (labelledNodes.contains(label) && labelledNodes(label).nonEmpty)
        labelledNodes(label).slice(skip, skip + limit).map(_.toGraphNode)
      else List.empty
    } else {
      if (labelledNodes.contains(label) && labelledNodes(label).nonEmpty) {
        filterNodesByMatchingParams(label = label, params = params).slice(skip, skip + limit).map(_.toGraphNode)
      } else List.empty
    }
  }

  def fetchNodes(label: String, matchPattern: MatchPattern): Iterable[GraphNode] = {
    filterByPattern(label, matchPattern).map(_.toGraphNode)
  }

  def fetchNodesSelect(
      label: String,
      select: Set[String],
      where: MatchPattern,
      skip: Int,
      limit: Int
  ): Iterable[PartialGraphNode] = {
    filterByPattern(label, where).slice(skip, skip + limit).map(_.reduceWith(select))
  }

  def fetchByNodeId(id: NodeId): Option[GraphNode] = buffer.indexedNodes.get(id).map(_.toGraphNode)

  def fetchCount(label: String, matchPattern: MatchPattern): Int = {
    labelledNodes
      .getOrElse(label, List.empty)
      .count(nodesForLable => matchPattern.eval(nodesForLable.params))
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

  private def filterByPattern(label: String, matchPattern: MatchPattern): Iterable[InternalNode] = {
    if (labelledNodes.contains(label) && labelledNodes(label).nonEmpty) {
      if(matchPattern == EmptyPattern()) {
        labelledNodes(label)
      } else labelledNodes(label).filter(node => matchPattern.eval(node.params))
    } else List.empty
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

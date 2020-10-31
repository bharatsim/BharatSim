package com.bharatsim.engine.graph

import com.bharatsim.engine.graph.GraphProvider.NodeId

class GraphNodeImpl(override val label: String, id: NodeId, params: Map[String, Any] = Map.empty) extends GraphNode {
  override def Id: NodeId = id

  override def apply(key: String): Option[Any] = params.get(key)

  override def getParams: Map[String, Any] = params
}

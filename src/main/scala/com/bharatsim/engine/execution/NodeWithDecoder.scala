package com.bharatsim.engine.execution

import com.bharatsim.engine.basicConversions.decoders.BasicMapDecoder
import com.bharatsim.engine.graph.GraphNode
import com.bharatsim.engine.models.Agent

case class NodeWithDecoder[T <: Agent](graphNode: GraphNode, decoder: BasicMapDecoder[T]) {
  def toAgent: Agent = {
    graphNode.as[T](decoder)
  }
}

object NodeWithDecoder {
  type GenericNodeWithDecoder = NodeWithDecoder[_ <: Agent]
}

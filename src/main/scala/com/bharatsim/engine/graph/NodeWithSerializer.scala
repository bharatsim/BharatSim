package com.bharatsim.engine.graph

import com.bharatsim.engine.basicConversions.BasicConversions.encode
import com.bharatsim.engine.basicConversions.encoders.BasicMapEncoder
import com.bharatsim.engine.models.Node
import com.bharatsim.engine.utils.Utils.fetchClassName

import scala.reflect.ClassTag

case class NodeWithSerializer[T <: Node: ClassTag](node: T, serializer: BasicMapEncoder[T]) {
  def serialize: Map[String, Any] = encode(node)(serializer)

  def label: String = fetchClassName[T]
}

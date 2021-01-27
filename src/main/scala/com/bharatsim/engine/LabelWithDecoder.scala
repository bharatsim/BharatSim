package com.bharatsim.engine

import com.bharatsim.engine.basicConversions.decoders.BasicMapDecoder
import com.bharatsim.engine.models.Agent

case class LabelWithDecoder[T <: Agent](label: String, decoder: BasicMapDecoder[T])

object LabelWithDecoder {
  type GenericLabelWithDecoder = LabelWithDecoder[_ <: Agent]
}

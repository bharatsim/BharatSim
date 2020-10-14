package com.bharatsim.engine.basicConversions

import com.bharatsim.engine.basicConversions.BasicValue.fromMap
import com.bharatsim.engine.basicConversions.decoders.BasicMapDecoder
import com.bharatsim.engine.basicConversions.encoders.BasicMapEncoder

object BasicConversions {
  def encode[T](o: T)(implicit encoder: BasicMapEncoder[T]): Map[String, Any] = {
    encoder.encode(o).toMap
  }

  def decode[T](m: Map[String, Any])(implicit decoder: BasicMapDecoder[T]): T = {
    decoder.decode(MapValue(fromMap(m)))
  }
}

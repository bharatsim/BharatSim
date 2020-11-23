package com.bharatsim.engine.basicConversions

import com.bharatsim.engine.basicConversions.BasicValue.fromMap
import com.bharatsim.engine.basicConversions.decoders.BasicMapDecoder
import com.bharatsim.engine.basicConversions.encoders.BasicMapEncoder

object BasicConversions {
  /**
   *
   * @param o object to encode
   * @param encoder implicit [[BasicMapEncoder]] for object
   * @tparam T type of object
   * @return encoded value of the object
   */
  def encode[T](o: T)(implicit encoder: BasicMapEncoder[T]): Map[String, Any] = {
    encoder.encode(o).toMap
  }

  /**
   *
   * @param m map to decode
   * @param decoder implicit [[BasicMapDecoder]] for type T
   * @tparam T expected return value type
   * @return object of type T
   */
  def decode[T](m: Map[String, Any])(implicit decoder: BasicMapDecoder[T]): T = {
    decoder.decode(MapValue(fromMap(m)))
  }
}

package com.bharatsim.engine.basicConversions.decoders

import com.bharatsim.engine.basicConversions.BasicValue.fromMap
import com.bharatsim.engine.basicConversions.{BasicValue, MapValue}


trait BasicDecoder[T] {
  def decode(b: BasicValue): T
}

object BasicDecoder {
  def instance[T](f: BasicValue => T): BasicDecoder[T] = (b: BasicValue) => f(b)
}

trait BasicMapDecoder[T] extends BasicDecoder[T] {
  def decode(b: BasicValue): T
}

object BasicMapDecoder {
  def decodeMap[T](m: Map[String, Any])(implicit decoder: BasicMapDecoder[T]): T = decoder.decode(MapValue(fromMap(m)))

  def apply[T](implicit decoder: BasicMapDecoder[T]): BasicMapDecoder[T] = decoder
}


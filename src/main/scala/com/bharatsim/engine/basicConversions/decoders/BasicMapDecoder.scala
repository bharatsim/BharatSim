package com.bharatsim.engine.basicConversions.decoders

import com.bharatsim.engine.basicConversions.BasicValue


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
  def apply[T](implicit decoder: BasicMapDecoder[T]): BasicMapDecoder[T] = decoder
}


package com.bharatsim.engine.basicConversions.encoders

import com.bharatsim.engine.basicConversions.MapValue

trait BasicMapEncoder[T] extends BasicEncoder[T] {
  def encode(o: T): MapValue
}

object BasicMapEncoder {
  def instance[T](fn: T => MapValue): BasicMapEncoder[T] = (o: T) => fn(o)
}

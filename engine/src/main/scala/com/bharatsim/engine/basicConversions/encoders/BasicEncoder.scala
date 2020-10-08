package com.bharatsim.engine.basicConversions.encoders

import com.bharatsim.engine.basicConversions.{BasicValue, MapValue}

trait BasicEncoder[T] {
  def encode(o: T): BasicValue
}

object BasicEncoder {
  def apply[T](implicit encoder: BasicEncoder[T]): BasicEncoder[T] = encoder

  def instance[T](func: T => BasicValue): BasicEncoder[T] = o => func(o)

  def toBasicMap[T](o: T)(implicit encoder: BasicMapEncoder[T]): MapValue = {
    encoder.encode(o)
  }
}

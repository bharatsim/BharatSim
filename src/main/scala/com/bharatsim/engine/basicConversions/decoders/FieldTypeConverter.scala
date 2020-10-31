package com.bharatsim.engine.basicConversions.decoders

import com.bharatsim.engine.basicConversions.BasicValue
import shapeless.labelled.FieldType

trait FieldTypeConverter[K <: Symbol, V] {
  def convert(m: Map[String, BasicValue]): FieldType[K, V]
}

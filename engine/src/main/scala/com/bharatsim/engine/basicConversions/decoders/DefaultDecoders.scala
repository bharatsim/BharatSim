package com.bharatsim.engine.basicConversions.decoders

import com.bharatsim.engine.basicConversions._
import shapeless.labelled.{FieldBuilder, FieldType}
import shapeless.{::, HList, HNil, LabelledGeneric, Lazy, Witness}


object DefaultDecoders {
  implicit val hnilMapConverter: MapConverter[HNil] = (_: Map[String, BasicValue]) => HNil

  implicit val basicIntDecoder: BasicDecoder[Int] = {
    case IntValue(v) => v
    case _ => throw new RuntimeException("Cannot convert to Int value, implement BasicDecoder to override default")
  }

  implicit val basicFloatDecoder: BasicDecoder[Float] = {
    case FloatValue(v) => v
    case _ => throw new RuntimeException("Cannot convert to Float value, implement BasicDecoder to override default")
  }

  implicit val basicDoubleDecoder: BasicDecoder[Double] = {
    case DoubleValue(v) => v
    case _ => throw new RuntimeException("Cannot convert to Double value, implement BasicDecoder to override default")
  }

  implicit val basicByteDecoder: BasicDecoder[Byte] = {
    case ByteValue(v) => v
    case _ => throw new RuntimeException("Cannot convert to Byte value, implement BasicDecoder to override default")
  }

  implicit val basicCharDecoder: BasicDecoder[Char] = {
    case CharValue(v) => v
    case _ => throw new RuntimeException("Cannot convert to Char value, implement BasicDecoder to override default")
  }

  implicit val basicStringDecoder: BasicDecoder[String] = {
    case StringValue(v) => v
    case _ => throw new RuntimeException("Cannot convert to String value, implement BasicDecoder to override default")
  }

  implicit def basicMapDecoder[V](implicit vDecoder: BasicDecoder[V]): BasicDecoder[Map[String, V]] = {
    case MapValue(m) => m.map(kv => {
      val value = kv._2
      (kv._1, vDecoder.decode(value))
    })
    case _ => throw new RuntimeException("Cannot convert to Map value, implement BasicDecoder to override default")
  }

  implicit def basicListDecoder[V](implicit vDecoder: BasicDecoder[V]): BasicDecoder[List[V]] = {
    case ListValue(m) => m.map(vDecoder.decode)
    case _ => throw new RuntimeException("Cannot convert to Map value, implement BasicDecoder to override default")
  }

  implicit def fieldTypeConverterProvider[K <: Symbol, V](implicit witness: Witness.Aux[K],
                                                          decoder: BasicDecoder[V]): FieldTypeConverter[K, V] = {
    (m: Map[String, BasicValue]) => {
      val fieldName = witness.value.name
      new FieldBuilder[K].apply(decoder.decode(m(fieldName)))
    }
  }

  implicit def toHList[K <: Symbol, V, T <: HList](implicit hConverter: Lazy[FieldTypeConverter[K, V]],
                                                   tConverter: MapConverter[T]): MapConverter[FieldType[K, V] :: T] = {
    (m: Map[String, BasicValue]) => {
      val head = hConverter.value.convert(m)
      val tail = tConverter.convert(m)
      head :: tail
    }
  }

  implicit def mapDecoder[T, H <: HList](implicit gen: LabelledGeneric.Aux[T, H],
                                         mpToHList: Lazy[MapConverter[H]]): BasicMapDecoder[T] = {
    case x: MapValue => gen.from(mpToHList.value.convert(x.m))
    case _ => throw new RuntimeException("Failed while creating BasicMapDecoder as provided value is not a MapValue")
  }
}

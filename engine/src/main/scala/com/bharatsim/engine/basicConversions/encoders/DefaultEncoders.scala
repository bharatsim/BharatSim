package com.bharatsim.engine.basicConversions.encoders

import com.bharatsim.engine.basicConversions._
import shapeless.labelled.FieldType
import shapeless.{::, HList, HNil, Lazy, Witness}

object DefaultEncoders {
  implicit val intEncoder: BasicEncoder[Int] = (o: Int) => IntValue(o)

  implicit val floatEncoder: BasicEncoder[Float] = (o: Float) => FloatValue(o)

  implicit val doubleEncoder: BasicEncoder[Double] = (o: Double) => DoubleValue(o)

  implicit val byteEncoder: BasicEncoder[Byte] = (o: Byte) => ByteValue(o)

  implicit val charEncoder: BasicEncoder[Char] = (o: Char) => CharValue(o)

  implicit val stringEncoder: BasicEncoder[String] = (o: String) => StringValue(o)

  implicit val hnilEncoder: BasicMapEncoder[HNil] = _ => MapValue(Map.empty)

  implicit def listEncoder[T](implicit encoder: BasicEncoder[T]): BasicEncoder[List[T]] =
    x => ListValue(x.map(v => encoder.encode(v)))

  implicit def mapEncoder[T](implicit encoder: BasicEncoder[T]): BasicEncoder[Map[String, T]] = {
    x => MapValue(x.map(kv => (kv._1, encoder.encode(kv._2))))
  }

  implicit def optionEncoder[T](implicit encoder: BasicEncoder[T]): BasicEncoder[Option[T]] = {
    opt => {
      if (opt.isDefined) encoder.encode(opt.get)
      else NoValue()
    }
  }

  implicit def hlistEncoder[K <: Symbol, H, T <: HList](implicit witness: Witness.Aux[K],
                                                        hEncoder: Lazy[BasicEncoder[H]],
                                                        tEncoder: BasicMapEncoder[T]): BasicMapEncoder[FieldType[K, H] :: T] = {

    val fieldName = witness.value.name

    hlist => {
      val head = hEncoder.value.encode(hlist.head)
      val tail = tEncoder.encode(hlist.tail)
      MapValue(tail.m + (fieldName -> head))
    }
  }

  implicit def genericEncoder[A, H](implicit
                                    gen: shapeless.LabelledGeneric.Aux[A, H],
                                    encoder: Lazy[BasicMapEncoder[H]]): BasicMapEncoder[A] = {
    a => encoder.value.encode(gen.to(a))
  }
}

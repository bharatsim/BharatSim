package com.bharatsim.engine.basicConversions

sealed trait BasicValue {
  def get: Any
}

case class IntValue(v: Int) extends BasicValue {
  override def get: Any = v
}

case class FloatValue(v: Float) extends BasicValue {
  override def get: Any = v
}

case class DoubleValue(v: Double) extends BasicValue {
  override def get: Any = v
}

case class LongValue(v: Long) extends BasicValue {
  override def get: Any = v
}

case class ByteValue(v: Byte) extends BasicValue {
  override def get: Any = v
}

case class CharValue(v: Char) extends BasicValue {
  override def get: Any = v
}

case class StringValue(v: String) extends BasicValue {
  override def get: Any = v
}

case class BooleanValue(v: Boolean) extends BasicValue {
  override def get: Any = v
}

case class ListValue(v: List[BasicValue]) extends BasicValue {
  def ++(a: BasicValue): ListValue = copy(a :: v)

  def toList: List[Any] = {
    v.map(x => BasicValue.toSimplifiedValue(x))
  }

  override def get: Any = v
}

case class MapValue(m: Map[String, BasicValue]) extends BasicValue {
  def toMap: Map[String, Any] = {
    m.map(kv => {
      val value = kv._2
      val simplifiedValue = BasicValue.toSimplifiedValue(value)
      (kv._1, simplifiedValue)
    })
  }

  override def get: Any = m
}

case class NoValue() extends BasicValue {
  override def get: Any = null
}

private[basicConversions] object BasicValue {
  def toSimplifiedValue(value: BasicValue): Any = {
    value match {
      case IntValue(v) => v
      case FloatValue(v) => v
      case DoubleValue(v) => v
      case LongValue(v) => v

      case BooleanValue(v) => v
      case ByteValue(v) => v
      case CharValue(v) => v
      case StringValue(v) => v

      case x: ListValue => x.toList
      case x: MapValue => x.toMap

      case NoValue() => null
    }
  }

  def fromMap(m: Map[String, Any]): Map[String, BasicValue] = {
    m.map(kv => {
      val value = kv._2
      val typedValue: BasicValue = fromAnyValue(value)

      (kv._1, typedValue)
    })
  }

  def fromList(list: List[_]): List[BasicValue] = {
    list.map(x => fromAnyValue(x))
  }

  private def fromAnyValue(value: Any): BasicValue = {
    value match {
      case x: Int => IntValue(x)
      case x: Float => FloatValue(x)
      case x: Double => DoubleValue(x)
      case x: Long => LongValue(x)

      case x: Boolean => BooleanValue(x)
      case x: Byte => ByteValue(x)
      case x: Char => CharValue(x)
      case x: String => StringValue(x)

      case x: List[Any] => ListValue(fromList(x))
      case x: Map[String, Any] => MapValue(fromMap(x))
      case null => NoValue()
    }
  }
}

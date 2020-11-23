package com.bharatsim.engine.basicConversions

sealed trait BasicValue {
  /**
   * @return internal value of that the BasicValue is holding
   */
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
  /**
   *
   * @param a [[BasicValue]] to be added to the list construct
   * @return new [[ListValue]] adding the element to the List construct
   */
  def ++(a: BasicValue): ListValue = copy(a :: v)

  /**
   *
   * @return [[List]] containing all the elements of the List construct
   */
  def toList: List[Any] = {
    v.map(x => BasicValue.toSimplifiedValue(x))
  }

  override def get: Any = v
}

case class MapValue(m: Map[String, BasicValue]) extends BasicValue {
  /**
   *
   * @return [[Map]] containing the all the key-value pairs in the Map construct
   */
  def toMap: Map[String, Any] = {
    m.map(kv => {
      val value = kv._2
      val simplifiedValue = BasicValue.toSimplifiedValue(value)
      (kv._1, simplifiedValue)
    })
  }

  override def get: Any = m
}

/**
 * Represents absence of the value, `get` on this will return null
 */
case class NoValue() extends BasicValue {
  override def get: Any = null
}

private[engine] object BasicValue {
  /**
   * Utility function that can be used to unbox the value held by the [[BasicValue]]
   * @param value the [[BasicValue]] to unbox
   * @return [[Any]] type value representing the unboxed [[BasicValue]]
   */
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

  def fromAnyValue(value: Any): BasicValue = {
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

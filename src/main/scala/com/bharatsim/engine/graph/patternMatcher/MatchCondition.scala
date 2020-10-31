package com.bharatsim.engine.graph.patternMatcher

import com.bharatsim.engine.basicConversions._
import com.bharatsim.engine.basicConversions.encoders.BasicEncoder

sealed trait MatchCondition extends Expression {
  def eval(m: Map[String, Any]): Boolean
}

object MatchCondition {
  implicit def toPattern(m: MatchCondition): MatchPattern = Pattern(m)

  implicit def toBasicValue[T](a: T)(implicit basicEncoder: BasicEncoder[T]): BasicValue = basicEncoder.encode(a)

  implicit class ToMatchCondition(a: String) {
    def equ(b: BasicValue): MatchCondition = Equals(b, a)

    def lt(b: BasicValue): MatchCondition = LessThan(b, a)

    def gt(b: BasicValue): MatchCondition = GreaterThan(b, a)

    def lte(b: BasicValue): MatchCondition = LessThanEquals(b, a)

    def gte(b: BasicValue): MatchCondition = GreaterThanEquals(b, a)
  }
}

case class Equals[B <: BasicValue](b: B, key: String) extends MatchCondition {
  override def eval(m: Map[String, Any]): Boolean = {
    val a: BasicValue = BasicValue.fromAnyValue(m.getOrElse(key, null))
    a.get == b.get
  }
}

case class LessThan[B <: BasicValue](b: B, key: String) extends MatchCondition {
  override def eval(m: Map[String, Any]): Boolean = {
    val a: BasicValue = BasicValue.fromAnyValue(m.getOrElse(key, NoValue))
    (a, b) match {
      case (a: IntValue, b: IntValue) => a.v < b.v
      case (a: FloatValue, b: FloatValue) => a.v < b.v
      case (a: DoubleValue, b: DoubleValue) => a.v < b.v
      case (a: LongValue, b: LongValue) => a.v < b.v
      case (a: ByteValue, b: ByteValue) => a.v < b.v
      case (a: CharValue, b: CharValue) => a.v < b.v
      case (a: StringValue, b: StringValue) => a.v < b.v
      case _ => throw new Exception(s"Cannot decide if ${a.get} < ${b.get}")
    }
  }
}

case class GreaterThan[B <: BasicValue](b: B, key: String) extends MatchCondition {
  override def eval(m: Map[String, Any]): Boolean = {
    val a: BasicValue = BasicValue.fromAnyValue(m.getOrElse(key, NoValue))
    (a, b) match {
      case (a: IntValue, b: IntValue) => a.v > b.v
      case (a: FloatValue, b: FloatValue) => a.v > b.v
      case (a: DoubleValue, b: DoubleValue) => a.v > b.v
      case (a: LongValue, b: LongValue) => a.v > b.v
      case (a: ByteValue, b: ByteValue) => a.v > b.v
      case (a: CharValue, b: CharValue) => a.v > b.v
      case (a: StringValue, b: StringValue) => a.v > b.v
      case _ => throw new Exception(s"Cannot decide if ${a.get} > ${b.get}")
    }
  }
}

case class LessThanEquals[B <: BasicValue](b: B, key: String) extends MatchCondition {
  override def eval(m: Map[String, Any]): Boolean = {
    val a: BasicValue = BasicValue.fromAnyValue(m.getOrElse(key, NoValue))
    (a, b) match {
      case (a: IntValue, b: IntValue) => a.v <= b.v
      case (a: FloatValue, b: FloatValue) => a.v <= b.v
      case (a: DoubleValue, b: DoubleValue) => a.v <= b.v
      case (a: LongValue, b: LongValue) => a.v <= b.v
      case (a: ByteValue, b: ByteValue) => a.v <= b.v
      case (a: CharValue, b: CharValue) => a.v <= b.v
      case (a: StringValue, b: StringValue) => a.v <= b.v
      case _ => throw new Exception(s"Cannot decide if ${a.get} <= ${b.get}")
    }
  }
}

case class GreaterThanEquals[B <: BasicValue](b: B, key: String) extends MatchCondition {
  override def eval(m: Map[String, Any]): Boolean = {
    val a: BasicValue = BasicValue.fromAnyValue(m.getOrElse(key, NoValue))
    (a, b) match {
      case (a: IntValue, b: IntValue) => a.v >= b.v
      case (a: FloatValue, b: FloatValue) => a.v >= b.v
      case (a: DoubleValue, b: DoubleValue) => a.v >= b.v
      case (a: LongValue, b: LongValue) => a.v >= b.v
      case (a: ByteValue, b: ByteValue) => a.v >= b.v
      case (a: CharValue, b: CharValue) => a.v >= b.v
      case (a: StringValue, b: StringValue) => a.v >= b.v
      case _ => throw new Exception(s"Cannot decide if ${a.get} >= ${b.get}")
    }
  }
}

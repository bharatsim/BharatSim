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

case class Equals[B](b: B, key: String) extends MatchCondition {
  override def eval(m: Map[String, Any]): Boolean = {
    m.getOrElse(key, null) == b
  }
}

case class LessThan[B](b: B, key: String) extends MatchCondition {
  override def eval(m: Map[String, Any]): Boolean = {
    val a = m.getOrElse(key, null)
    (a, b) match {
      case (a: Int, b: Int)       => a < b
      case (a: Float, b: Float)   => a < b
      case (a: Double, b: Double) => a < b
      case (a: Long, b: Long)     => a < b
      case (a: Byte, b: Byte)     => a < b
      case (a: Char, b: Char)     => a < b
      case (a: String, b: String) => a < b
      case (null, _)              => false
      case _                      => throw new Exception(s"Cannot decide if $a < $b")
    }
  }
}

case class GreaterThan[B](b: B, key: String) extends MatchCondition {
  override def eval(m: Map[String, Any]): Boolean = {
    val a = m.getOrElse(key, null)
    (a, b) match {
      case (a: Int, b: Int)       => a > b
      case (a: Float, b: Float)   => a > b
      case (a: Double, b: Double) => a > b
      case (a: Long, b: Long)     => a > b
      case (a: Byte, b: Byte)     => a > b
      case (a: Char, b: Char)     => a > b
      case (a: String, b: String) => a > b
      case (null, _)              => false
      case _                      => throw new Exception(s"Cannot decide if $a > $b")
    }
  }
}

case class LessThanEquals[B](b: B, key: String) extends MatchCondition {
  override def eval(m: Map[String, Any]): Boolean = {
    val a = m.getOrElse(key, null)
    (a, b) match {
      case (a: Int, b: Int)       => a <= b
      case (a: Float, b: Float)   => a <= b
      case (a: Double, b: Double) => a <= b
      case (a: Long, b: Long)     => a <= b
      case (a: Byte, b: Byte)     => a <= b
      case (a: Char, b: Char)     => a <= b
      case (a: String, b: String) => a <= b
      case (null, _)              => false
      case _                      => throw new Exception(s"Cannot decide if $a <= $b")
    }
  }
}

case class GreaterThanEquals[B](b: B, key: String) extends MatchCondition {
  override def eval(m: Map[String, Any]): Boolean = {
    val a = m.getOrElse(key, null)
    (a, b) match {
      case (a: Int, b: Int)       => a >= b
      case (a: Float, b: Float)   => a >= b
      case (a: Double, b: Double) => a >= b
      case (a: Long, b: Long)     => a >= b
      case (a: Byte, b: Byte)     => a >= b
      case (a: Char, b: Char)     => a >= b
      case (a: String, b: String) => a >= b
      case (null, _)              => false
      case _                      => throw new Exception(s"Cannot decide if $a >= $b")
    }
  }
}

object Equals {
  def apply[B <: BasicValue](b: B, key: String): Equals[b.Out] = new Equals(b.get, key)
}

object LessThan {
  def apply[B <: BasicValue](b: B, key: String): LessThan[b.Out] = new LessThan(b.get, key)
}

object GreaterThan {
  def apply[B <: BasicValue](b: B, key: String): GreaterThan[b.Out] = new GreaterThan(b.get, key)
}

object LessThanEquals {
  def apply[B <: BasicValue](b: B, key: String): LessThanEquals[b.Out] = new LessThanEquals(b.get, key)
}

object GreaterThanEquals {
  def apply[B <: BasicValue](b: B, key: String): GreaterThanEquals[b.Out] = new GreaterThanEquals(b.get, key)
}

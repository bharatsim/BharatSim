package com.bharatsim.engine.graph.patternMatcher

import com.fasterxml.jackson.annotation.{JsonSubTypes, JsonTypeInfo}

@JsonTypeInfo(use = JsonTypeInfo.Id.NAME)
@JsonSubTypes(
  Array(
    new JsonSubTypes.Type(value = classOf[Pattern], name = "pattern"),
    new JsonSubTypes.Type(value = classOf[AndPattern], name = "and"),
    new JsonSubTypes.Type(value = classOf[OrPattern], name = "or"),
    new JsonSubTypes.Type(value = classOf[EmptyPattern], name = "empty")
  )
)
sealed trait MatchPattern extends Expression {
  def or(b: MatchPattern): OrPattern = OrPattern(this, b)

  def and(b: MatchPattern): AndPattern = AndPattern(this, b)
}

case class Pattern(a: Expression) extends MatchPattern {
  override def eval(m: Map[String, Any]): Boolean = a.eval(m)
}

case class AndPattern(a: Expression, b: Expression) extends MatchPattern {
  override def eval(m: Map[String, Any]): Boolean = a.eval(m) && b.eval(m)
}

case class OrPattern(a: Expression, b: Expression) extends MatchPattern {
  override def eval(m: Map[String, Any]): Boolean = a.eval(m) || b.eval(m)
}

case class EmptyPattern() extends MatchPattern {
  override def eval(m: Map[String, Any]): Boolean = true
}

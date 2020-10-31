package com.bharatsim.engine.graph.patternMatcher

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


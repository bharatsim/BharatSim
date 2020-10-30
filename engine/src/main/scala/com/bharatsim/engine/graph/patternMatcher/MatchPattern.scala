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

object MatchPattern {
  def toString(m: MatchPattern, variable: String): String = {
    m match {
      case Pattern(a) => baseCase(a, variable)
      case AndPattern(a, b) => "(" + baseCase(a, variable) + " and " + baseCase(b, variable) + ")"
      case OrPattern(a, b) => "(" + baseCase(a, variable) + " or " + baseCase(b, variable) + ")"
    }
  }

  private def baseCase(a: Expression, variable: String): String = {
    a match {
      case mp: MatchPattern => toString(mp, variable)
      case mc: MatchCondition => MatchCondition.toString(mc, variable)
    }
  }
}

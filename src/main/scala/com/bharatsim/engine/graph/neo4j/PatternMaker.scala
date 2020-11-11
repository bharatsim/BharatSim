package com.bharatsim.engine.graph.neo4j

import com.bharatsim.engine.basicConversions.{BasicValue, StringValue}
import com.bharatsim.engine.graph.patternMatcher._

private[engine] object PatternMaker {
  def from(m: MatchPattern, variable: String): String = {
    m match {
      case Pattern(a)       => baseCase(a, variable)
      case AndPattern(a, b) => "(" + baseCase(a, variable) + " and " + baseCase(b, variable) + ")"
      case OrPattern(a, b)  => "(" + baseCase(a, variable) + " or " + baseCase(b, variable) + ")"
    }
  }

  private def baseCase(a: Expression, variable: String): String = {
    a match {
      case mp: MatchPattern   => from(mp, variable)
      case mc: MatchCondition => condition(mc, variable)
    }
  }

  private def condition(mc: MatchCondition, variable: String): String = {
    mc match {
      case Equals(b, key)            => s"$variable.$key = ${value(b)}"
      case LessThan(b, key)          => s"$variable.$key < ${value(b)}"
      case GreaterThan(b, key)       => s"$variable.$key > ${value(b)}"
      case LessThanEquals(b, key)    => s"$variable.$key <= ${value(b)}"
      case GreaterThanEquals(b, key) => s"$variable.$key >= ${value(b)}"
    }
  }

  private def value(x: BasicValue): String = {
    x match {
      case StringValue(v) => s""""$v""""
      case _              => s"${x.get}"
    }
  }

}

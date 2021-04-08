package com.bharatsim.engine.graph.neo4j

import com.bharatsim.engine.graph.patternMatcher._

case class PatternWithParams(pattern: String, params: Map[String, Any])

private[engine] object PatternMaker {
  def from(m: MatchPattern, variable: String): PatternWithParams = {
    from(m, variable, VariableGenerator())._1
  }

  private def from(
      m: MatchPattern,
      variable: String,
      variableGenerator: VariableGenerator
  ): (PatternWithParams, VariableGenerator) = {
    m match {
      case Pattern(a) => baseCase(a, variable, variableGenerator)
      case AndPattern(a, b) =>
        val (resolvedA, vgA) = baseCase(a, variable, variableGenerator)
        val (resolvedB, vgB) = baseCase(b, variable, vgA)
        val patternString = s"(${resolvedA.pattern} AND ${resolvedB.pattern})"
        val params = resolvedA.params ++ resolvedB.params
        (PatternWithParams(patternString, params), vgB)
      case OrPattern(a, b) =>
        val (resolvedA, vgA) = baseCase(a, variable, variableGenerator)
        val (resolvedB, vgB) = baseCase(b, variable, vgA)
        val patternString = s"(${resolvedA.pattern} OR ${resolvedB.pattern})"
        val params = resolvedA.params ++ resolvedB.params
        (PatternWithParams(patternString, params), vgB)

      case EmptyPattern() => (PatternWithParams("", Map()), variableGenerator)
    }
  }

  private def baseCase(
      a: Expression,
      variable: String,
      variableGenerator: VariableGenerator
  ): (PatternWithParams, VariableGenerator) = {
    a match {
      case mp: MatchPattern => from(mp, variable, variableGenerator)
      case mc: MatchCondition =>
        val generatedVariable = variableGenerator.generate
        (condition(mc, variable, generatedVariable), variableGenerator.next)
    }
  }

  private def condition(mc: MatchCondition, variable: String, generatedVariable: String): PatternWithParams = {
    mc match {
      case Equals(b, key) =>
        PatternWithParams(s"$variable.$key = $$$generatedVariable", Map(generatedVariable -> b.get))
      case LessThan(b, key) =>
        PatternWithParams(s"$variable.$key < $$$generatedVariable", Map(generatedVariable -> b.get))
      case GreaterThan(b, key) =>
        PatternWithParams(s"$variable.$key > $$$generatedVariable", Map(generatedVariable -> b.get))
      case LessThanEquals(b, key) =>
        PatternWithParams(s"$variable.$key <= $$$generatedVariable", Map(generatedVariable -> b.get))
      case GreaterThanEquals(b, key) =>
        PatternWithParams(s"$variable.$key >= $$$generatedVariable", Map(generatedVariable -> b.get))
    }
  }
}

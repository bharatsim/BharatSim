package com.bharatsim.engine.graph.neo4j

import com.bharatsim.engine.graph.patternMatcher._

case class PatternWithParams(pattern: String, params: Map[String, Any])

private[engine] object PatternMaker {
  def from(m: MatchPattern, variable: String, propsPrefix: Option[String] = None): PatternWithParams = {
    from(m, variable, VariableGenerator(), propsPrefix)._1
  }

  private def from(
      m: MatchPattern,
      variable: String,
      variableGenerator: VariableGenerator,
      propsPrefix: Option[String]
  ): (PatternWithParams, VariableGenerator) = {
    m match {
      case Pattern(a) => baseCase(a, variable, variableGenerator, propsPrefix)
      case AndPattern(a, b) =>
        val (resolvedA, vgA) = baseCase(a, variable, variableGenerator, propsPrefix)
        val (resolvedB, vgB) = baseCase(b, variable, vgA, propsPrefix)
        val patternString = s"(${resolvedA.pattern} AND ${resolvedB.pattern})"
        val params = resolvedA.params ++ resolvedB.params
        (PatternWithParams(patternString, params), vgB)
      case OrPattern(a, b) =>
        val (resolvedA, vgA) = baseCase(a, variable, variableGenerator, propsPrefix)
        val (resolvedB, vgB) = baseCase(b, variable, vgA, propsPrefix)
        val patternString = s"(${resolvedA.pattern} OR ${resolvedB.pattern})"
        val params = resolvedA.params ++ resolvedB.params
        (PatternWithParams(patternString, params), vgB)

      case EmptyPattern() => (PatternWithParams("", Map()), variableGenerator)
    }
  }

  private def baseCase(
      a: Expression,
      variable: String,
      variableGenerator: VariableGenerator,
      propsPrefix: Option[String]
  ): (PatternWithParams, VariableGenerator) = {
    a match {
      case mp: MatchPattern => from(mp, variable, variableGenerator, propsPrefix)
      case mc: MatchCondition =>
        val generatedVariable = variableGenerator.generate
        (condition(mc, variable, generatedVariable, propsPrefix), variableGenerator.next)
    }
  }

  private def condition(mc: MatchCondition, variable: String, generatedVariable: String, propsPrefix: Option[String]): PatternWithParams = {
    val valueVariable = if(propsPrefix.isDefined) s"""${propsPrefix.get}.$generatedVariable""" else s"""$$$generatedVariable"""
    mc match {
      case Equals(b, key) =>
        PatternWithParams(s"$variable.$key = $valueVariable", Map(generatedVariable -> b.get))
      case LessThan(b, key) =>
        PatternWithParams(s"$variable.$key < $valueVariable", Map(generatedVariable -> b.get))
      case GreaterThan(b, key) =>
        PatternWithParams(s"$variable.$key > $valueVariable", Map(generatedVariable -> b.get))
      case LessThanEquals(b, key) =>
        PatternWithParams(s"$variable.$key <= $valueVariable", Map(generatedVariable -> b.get))
      case GreaterThanEquals(b, key) =>
        PatternWithParams(s"$variable.$key >= $valueVariable", Map(generatedVariable -> b.get))
    }
  }
}

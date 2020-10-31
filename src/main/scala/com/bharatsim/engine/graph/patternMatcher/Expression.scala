package com.bharatsim.engine.graph.patternMatcher

private[engine] trait Expression {
  def eval(m: Map[String, Any]): Boolean
}

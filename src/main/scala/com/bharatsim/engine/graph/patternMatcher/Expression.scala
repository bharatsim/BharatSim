package com.bharatsim.engine.graph.patternMatcher

import com.fasterxml.jackson.annotation.{JsonSubTypes, JsonTypeInfo}

@JsonTypeInfo(use = JsonTypeInfo.Id.NAME)
@JsonSubTypes(
  Array(
    new JsonSubTypes.Type(value = classOf[Equals[_]], name = "eq"),
    new JsonSubTypes.Type(value = classOf[GreaterThan[_]], name = "gt"),
    new JsonSubTypes.Type(value = classOf[LessThan[_]], name = "lt"),
    new JsonSubTypes.Type(value = classOf[GreaterThanEquals[_]], name = "gte"),
    new JsonSubTypes.Type(value = classOf[LessThanEquals[_]], name = "lte"),
    new JsonSubTypes.Type(value = classOf[AndPattern], name = "and"),
    new JsonSubTypes.Type(value = classOf[OrPattern], name = "or"),
    new JsonSubTypes.Type(value = classOf[EmptyPattern], name = "empty"),
    new JsonSubTypes.Type(value = classOf[Pattern], name = "pattern")
  )
)
private[engine] trait Expression {
  def eval(m: Map[String, Any]): Boolean
}

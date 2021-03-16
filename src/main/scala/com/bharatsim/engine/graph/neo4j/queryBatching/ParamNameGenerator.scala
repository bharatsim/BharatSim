package com.bharatsim.engine.graph.neo4j.queryBatching

case class ParamNameGenerator(c: Char = 'a', count: Int = 0) {
  def get: String = s"$c$count"

  def next: ParamNameGenerator = ParamNameGenerator(if (c == 'z') 'a' else (c + 1).toChar, if (c == 'z') 0 else count)
}


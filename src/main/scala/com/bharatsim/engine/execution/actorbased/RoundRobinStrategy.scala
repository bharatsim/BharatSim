package com.bharatsim.engine.execution.actorbased

class RoundRobinStrategy(limit: Int) {
  private var current = 0

  def next: Int = {
    val toReturn = current
    current = (current + 1) % limit
    toReturn
  }
}

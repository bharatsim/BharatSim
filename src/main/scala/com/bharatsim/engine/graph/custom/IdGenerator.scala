package com.bharatsim.engine.graph.custom

import java.util.concurrent.atomic.AtomicInteger

private[engine] class IdGenerator {
  private val lastGenerated = new AtomicInteger(0)

  def generateId: Int = {
    lastGenerated.incrementAndGet()
  }
}

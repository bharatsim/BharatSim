package com.bharatsim.engine

private[engine] class IdGenerator {
  private var lastGenerated = 0
  def generateId: Int = {
    lastGenerated += 1
    lastGenerated
  }
}

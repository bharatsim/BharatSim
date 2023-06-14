package com.bharatsim.examples.epidemiology.sir

import com.bharatsim.engine.Context
import com.bharatsim.engine.graph.patternMatcher.MatchCondition._
import com.bharatsim.engine.listeners.CSVSpecs
import com.bharatsim.examples.epidemiology.sir.InfectionStatus.{Susceptible, Infected, Removed}

class SIROutputSpec(context: Context) extends CSVSpecs {
  override def getHeaders: List[String] =
    List(
      "Step",
      "Susceptible",
      "Infected",
      "Removed"
    )

  override def getRows(): List[List[Any]] = {
    val graphProvider = context.graphProvider
    val label = "Person"
    val row = List(
      context.getCurrentStep,
      graphProvider.fetchCount(label, "infectionState" equ Susceptible),
      graphProvider.fetchCount(label, "infectionState" equ Infected),
      graphProvider.fetchCount(label, "infectionState" equ Removed)
    )
    List(row)
  }
}

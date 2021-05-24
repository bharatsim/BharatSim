package com.bharatsim.model12Hr

import com.bharatsim.engine.Context
import com.bharatsim.engine.graph.patternMatcher.MatchCondition._
import com.bharatsim.engine.listeners.CSVSpecs
import com.bharatsim.model.InfectionStatus.{
  Asymptomatic,
  Deceased,
  Exposed,
  InfectedMild,
  InfectedSevere,
  PreSymptomatic,
  Recovered,
  Susceptible
}

class SEIROutputSpec(context: Context) extends CSVSpecs {
  override def getHeaders: List[String] =
    List(
      "Step",
      "Susceptible",
      "Exposed",
      "PreSymptomatic",
      "InfectedMild",
      "InfectedSevere",
      "Asymptomatic",
      "Recovered",
      "Deceased"
    )

  override def getRows(): List[List[Any]] = {
    val graphProvider = context.graphProvider
    val label = "Person"
    val row = List(
      context.getCurrentStep,
      graphProvider.fetchCount(label, "infectionState" equ Susceptible),
      graphProvider.fetchCount(label, "infectionState" equ Exposed),
      graphProvider.fetchCount(label, "infectionState" equ PreSymptomatic),
      graphProvider.fetchCount(label, "infectionState" equ InfectedMild),
      graphProvider.fetchCount(label, "infectionState" equ InfectedSevere),
      graphProvider.fetchCount(label, "infectionState" equ Asymptomatic),
      graphProvider.fetchCount(label, "infectionState" equ Recovered),
      graphProvider.fetchCount(label, "infectionState" equ Deceased)
    )
    return List(row)

  }
}

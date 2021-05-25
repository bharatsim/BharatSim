package com.bharatsim.examples.epidemiology.tenCompartmentalModel

import com.bharatsim.engine.Context
import com.bharatsim.engine.graph.patternMatcher.MatchCondition._
import com.bharatsim.engine.listeners.CSVSpecs
import com.bharatsim.examples.epidemiology.tenCompartmentalModel.InfectionStatus.{
  Asymptomatic,
  Deceased,
  Exposed,
  Hospitalized,
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
      "Asymptomatic",
      "PreSymptomatic",
      "InfectedMild",
      "InfectedSevere",
      "Hospitalized",
      "Recovered",
      "Deceased",
      "TotalInfected",
      "TotalRemoved"
    )

  override def getRows(): List[List[Any]] = {
    val graphProvider = context.graphProvider
    val label = "Person"

    val susceptible = graphProvider.fetchCount(label, "infectionState" equ Susceptible)
    val exposed = graphProvider.fetchCount(label, "infectionState" equ Exposed)
    val asymptomatic = graphProvider.fetchCount(label, "infectionState" equ Asymptomatic)
    val preSymptomatic = graphProvider.fetchCount(label, "infectionState" equ PreSymptomatic)
    val infectedMild = graphProvider.fetchCount(label, "infectionState" equ InfectedMild)
    val infectedSevere = graphProvider.fetchCount(label, "infectionState" equ InfectedSevere)
    val hospitalized = graphProvider.fetchCount(label, "infectionState" equ Hospitalized)
    val recovered = graphProvider.fetchCount(label, "infectionState" equ Recovered)
    val deceased = graphProvider.fetchCount(label, "infectionState" equ Deceased)

    val row = List(
      context.getCurrentStep,
      susceptible,
      exposed,
      asymptomatic,
      preSymptomatic,
      infectedMild,
      infectedSevere,
      hospitalized,
      recovered,
      deceased,
      asymptomatic + preSymptomatic + infectedMild + infectedSevere + hospitalized,
      recovered + deceased
    )
    return List(row)

  }
}

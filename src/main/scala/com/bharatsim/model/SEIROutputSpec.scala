package com.bharatsim.model

import com.bharatsim.engine.Context
import com.bharatsim.engine.graph.patternMatcher.MatchCondition._
import com.bharatsim.engine.listeners.CSVSpecs
import com.bharatsim.model.InfectionStatus.{Asymptomatic, Deceased, Exposed, InfectedMild, InfectedSevere, PreSymptomatic, Recovered, Susceptible}

class SEIROutputSpec(context: Context) extends CSVSpecs {
  override def getHeaders: List[String] = List("Step", "Susceptible", "Exposed", "PreSymptomatic", "InfectedMild", "InfectedSevere", "Asymptomatic", "Recovered", "Deceased")

  override def getValue(fieldName: String): Any = {
    val graphProvider = context.graphProvider
    val label = "Person"
    fieldName match {
      case "Step" => context.getCurrentStep
      case "Susceptible" => graphProvider.fetchCount(label, "infectionState" equ Susceptible)
      case "Exposed" => graphProvider.fetchCount(label, "infectionState" equ Exposed)
      case "PreSymptomatic" => graphProvider.fetchCount(label, "infectionState" equ PreSymptomatic)
      case "InfectedMild" => graphProvider.fetchCount(label, "infectionState" equ InfectedMild)
      case "InfectedSevere" => graphProvider.fetchCount(label, "infectionState" equ InfectedSevere)
      case "Asymptomatic" => graphProvider.fetchCount(label, "infectionState" equ Asymptomatic)
      case "Recovered" => graphProvider.fetchCount(label, "infectionState" equ Recovered)
      case "Deceased" => graphProvider.fetchCount(label, "infectionState" equ Deceased)
    }
  }
}

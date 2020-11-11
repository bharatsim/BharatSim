package com.bharatsim.model

import com.bharatsim.engine.Context
import com.bharatsim.engine.graph.patternMatcher.MatchCondition._
import com.bharatsim.engine.listeners.CSVSpecs
import com.bharatsim.model.InfectionStatus.{Deceased, Exposed, Infected, Recovered, Susceptible}

class SEIROutputSpec(context: Context) extends CSVSpecs {
  override def getHeaders(): List[String] = List("Step", "Susceptible", "Exposed", "Infected", "Recovered", "Deceased")

  override def getValue(fieldName: String): Any = {
    val graphProvider = context.graphProvider
    val label = "Person"
    fieldName match {
      case "Step"        => context.getCurrentStep
      case "Susceptible" => graphProvider.fetchCount(label, "infectionState" equ Susceptible)
      case "Exposed"     => graphProvider.fetchCount(label, "infectionState" equ Exposed)
      case "Infected"    => graphProvider.fetchCount(label, "infectionState" equ Infected)
      case "Recovered"   => graphProvider.fetchCount(label, "infectionState" equ Recovered)
      case "Deceased"    => graphProvider.fetchCount(label, "infectionState" equ Deceased)
    }
  }
}

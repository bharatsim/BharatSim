package com.bharatsim.model

import com.bharatsim.engine.Context
import com.bharatsim.engine.listners.CSVSpecs

class SEIROutputSpec(context: Context) extends CSVSpecs {
  override def getHeaders(): List[String] = List("Step", "Susceptible", "Exposed", "Infected", "Recovered", "Deceased")
  override def getValue(fieldName: String): Any = {
    fieldName match {
      case "Step" => context.getCurrentStep
      case "Susceptible" => context.graphProvider.fetchNodes("Person", ("infectionState", "Susceptible")).size
      case "Exposed" => context.graphProvider.fetchNodes("Person", ("infectionState", "Exposed")).size
      case "Infected" => context.graphProvider.fetchNodes("Person", ("infectionState", "Infected")).size
      case "Recovered" => context.graphProvider.fetchNodes("Person", ("infectionState", "Recovered")).size
      case "Deceased" => context.graphProvider.fetchNodes("Person", ("infectionState", "Deceased")).size
    }
  }
}

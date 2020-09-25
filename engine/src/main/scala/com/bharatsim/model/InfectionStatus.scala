package com.bharatsim.model

object InfectionStatus extends Enumeration {
  type InfectionStatus = Value
  val Susceptible, Exposed, Infected, Recovered, Deceased = Value
}

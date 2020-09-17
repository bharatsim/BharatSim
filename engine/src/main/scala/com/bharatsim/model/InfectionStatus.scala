package com.bharatsim.model

object InfectionStatus extends Enumeration {
  type InfectionStatus = Value
  val Susceptible, Infected, Recovered = Value
}

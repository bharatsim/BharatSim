package com.bharatsim.examples.epidemiology.sir

import com.bharatsim.engine.basicConversions.StringValue
import com.bharatsim.engine.basicConversions.decoders.BasicDecoder
import com.bharatsim.engine.basicConversions.encoders.BasicEncoder

object InfectionStatus extends Enumeration {
  type InfectionStatus = Value
  val Susceptible, Infected, Removed = Value

  implicit val infectionStatusDecoder: BasicDecoder[InfectionStatus] = {
    case StringValue(v) => withName(v)
    case _ => throw new RuntimeException("Infection status was not stored as a string")
  }

  implicit val infectionStatusEncoder: BasicEncoder[InfectionStatus] = {
    case Susceptible => StringValue("Susceptible")
    case Infected => StringValue("Infected")
    case Removed => StringValue("Removed")
  }
}

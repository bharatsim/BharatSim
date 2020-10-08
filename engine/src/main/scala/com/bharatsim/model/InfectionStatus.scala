package com.bharatsim.model

import com.bharatsim.engine.basicConversions.StringValue
import com.bharatsim.engine.basicConversions.decoders.BasicDecoder
import com.bharatsim.engine.basicConversions.encoders.BasicEncoder

object InfectionStatus extends Enumeration {
  type InfectionStatus = Value
  val Susceptible, Exposed, Infected, Recovered, Deceased = Value

  implicit val infectionStatusDecoder: BasicDecoder[InfectionStatus] = {
    case StringValue(v) => withName(v)
    case _ => throw new RuntimeException("Infection status was not stored as a string")
  }

  implicit val infectionStatusEncoder: BasicEncoder[InfectionStatus] = {
    case Susceptible => StringValue("Susceptible")
    case Exposed => StringValue("Exposed")
    case Infected => StringValue("Infected")
    case Recovered => StringValue("Recovered")
    case Deceased => StringValue("Deceased")
  }
}

package com.bharatsim.model12Hr

import com.bharatsim.engine.basicConversions.StringValue
import com.bharatsim.engine.basicConversions.decoders.BasicDecoder
import com.bharatsim.engine.basicConversions.encoders.BasicEncoder

object InfectionStatus extends Enumeration {
  type InfectionStatus = Value
  val Susceptible, Exposed, PreSymptomatic, InfectedMild, InfectedSevere, Asymptomatic, Recovered, Deceased = Value

  implicit val infectionStatusDecoder: BasicDecoder[InfectionStatus] = {
    case StringValue(v) => withName(v)
    case _              => throw new RuntimeException("Infection status was not stored as a string")
  }

  implicit val infectionStatusEncoder: BasicEncoder[InfectionStatus] = {
    case Susceptible    => StringValue("Susceptible")
    case Exposed        => StringValue("Exposed")
    case PreSymptomatic => StringValue("PreSymptomatic")
    case InfectedMild   => StringValue("InfectedMild")
    case InfectedSevere => StringValue("InfectedSevere")
    case Asymptomatic   => StringValue("Asymptomatic")
    case Recovered      => StringValue("Recovered")
    case Deceased       => StringValue("Deceased")
  }
}

object InfectionSeverity extends Enumeration {
  type InfectionSeverity = Value
  val Mild, Severe = Value

  implicit val infectionSeverityDecoder: BasicDecoder[InfectionSeverity] = {
    case StringValue(v) => withName(v)
    case _              => throw new RuntimeException("Infection severity was not stored as a string")
  }

  implicit val infectionSeverityEncoder: BasicEncoder[InfectionSeverity] = {
    case Mild   => StringValue("Mild")
    case Severe => StringValue("Severe")
  }
}

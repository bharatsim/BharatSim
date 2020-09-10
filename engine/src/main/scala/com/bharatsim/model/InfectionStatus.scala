package com.bharatsim.model

abstract class InfectionStatus

case object Susceptible extends InfectionStatus
case object Exposed extends InfectionStatus
case object Infected extends InfectionStatus
case object Recovered extends InfectionStatus
case object Deceased extends InfectionStatus

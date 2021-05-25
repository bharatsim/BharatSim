package com.bharatsim.examples.epidemiology.tenCompartmentalModel

import com.bharatsim.engine.basicConversions.decoders.DefaultDecoders._
import com.bharatsim.engine.basicConversions.encoders.DefaultEncoders._
import com.bharatsim.engine.graph.GraphNode
import com.bharatsim.engine.models.{Network, StatefulAgent}
import com.bharatsim.examples.epidemiology.tenCompartmentalModel.InfectionSeverity.{Mild, Severe}
import com.bharatsim.examples.epidemiology.tenCompartmentalModel.InfectionStatus._
import com.bharatsim.examples.epidemiology.tenCompartmentalModel.diseaseState._

case class Person(
    id: Long,
    age: Double,
    infectionState: InfectionStatus,
    infectedAtTick: Int,
    takesPublicTransport: Boolean,
    isEssentialWorker: Boolean,
    violateLockdown: Boolean,
    village_town: String,
    lat: String,
    long: String,
    isEmployee: Boolean,
    isStudent: Boolean,
    betaMultiplier: Double,
    gamma: Double,
    delta: Double,
    sigma: Double,
    isHCW: Boolean,
    vaccinationStatus: Boolean = false
) extends StatefulAgent {

  def decodeNode(classType: String, node: GraphNode): Network = {
    classType match {
      case "House"       => node.as[House]
      case "Office"      => node.as[Office]
      case "School"      => node.as[School]
      case "Transport"   => node.as[Transport]
      case "PublicPlace" => node.as[PublicPlace]
      case "Hospital"    => node.as[Hospital]
    }
  }

  def isSusceptible: Boolean = activeState.isInstanceOf[SusceptibleState]

  def isExposed: Boolean = activeState.isInstanceOf[ExposedState]

  def isAsymptomatic: Boolean = activeState.isInstanceOf[AsymptomaticState]

  def isPreSymptomatic: Boolean = activeState.isInstanceOf[PreSymptomaticState]

  def isMildInfected: Boolean =
    activeState.isInstanceOf[InfectedState] && activeState.asInstanceOf[InfectedState].severity == Mild

  def isSevereInfected: Boolean =
    activeState.isInstanceOf[InfectedState] && activeState.asInstanceOf[InfectedState].severity == Severe

  def isHospitalized: Boolean = activeState.isInstanceOf[HospitalizedState]

  def isRecovered: Boolean = activeState.isInstanceOf[RecoveredState]

  def isDeceased: Boolean = activeState.isInstanceOf[DeceasedState]

  def isVaccinated: Boolean = this.vaccinationStatus

  def vaccinate(): Unit = updateParam("vaccinationStatus", true)

  def shouldGetVaccine(): Boolean =
    !this.vaccinationStatus && !(infectionState == InfectedMild || infectionState == InfectedSevere || infectionState == Hospitalized || infectionState == Deceased)

  def isHomeBound(): Boolean = !this.isStudent && !this.isEmployee

  addRelation[House]("STAYS_AT")
  addRelation[Office]("WORKS_AT")
  addRelation[School]("STUDIES_AT")
  addRelation[Transport]("TAKES")
  addRelation[PublicPlace]("VISITS")
  addRelation[Hospital]("TREATED_AT")
}

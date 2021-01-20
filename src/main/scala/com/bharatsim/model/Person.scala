package com.bharatsim.model

import com.bharatsim.engine.Context
import com.bharatsim.engine.basicConversions.decoders.DefaultDecoders._
import com.bharatsim.engine.basicConversions.encoders.DefaultEncoders._
import com.bharatsim.engine.graph.GraphNode
import com.bharatsim.engine.models.{Network, StatefulAgent}
import com.bharatsim.model.DayUtil.isEOD
import com.bharatsim.model.InfectionSeverity.{Mild, Severe}
import com.bharatsim.model.InfectionStatus._
import com.bharatsim.model.diseaseState._

case class Person(
    id: Int,
    age: Int,
    infectionState: InfectionStatus,
    infectionDay: Int,
    takesPublicTransport: Boolean,
    isEssentialWorker: Boolean,
    violateLockdown: Boolean,
    village_town: String,
    lat: String,
    long: String
) extends StatefulAgent {

  private def incrementInfectionDay(context: Context): Unit = {
    if ((!isSusceptible && !isRecovered && !isDeceased) && isEOD(context.getCurrentStep)) {
      updateParam("infectionDay", infectionDay + 1)
    }
  }

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

  def isPreSymptomatic: Boolean = activeState.isInstanceOf[PreSymptomaticState]

  def isMildInfected: Boolean = activeState == InfectedState(Mild)

  def isSevereInfected: Boolean = activeState == InfectedState(Severe)

  def isAsymptomatic: Boolean = activeState.isInstanceOf[AsymptomaticState]

  def isRecovered: Boolean = activeState.isInstanceOf[RecoveredState]

  def isDeceased: Boolean = activeState.isInstanceOf[DeceasedState]

  addBehaviour(incrementInfectionDay)

  addRelation[House]("STAYS_AT")
  addRelation[Office]("WORKS_AT")
  addRelation[School]("STUDIES_AT")
  addRelation[Transport]("TAKES")
  addRelation[PublicPlace]("VISITS")
  addRelation[Hospital]("ADMITTED_AT")
}

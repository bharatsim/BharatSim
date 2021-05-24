package com.bharatsim.model12Hr

import com.bharatsim.engine.basicConversions.decoders.DefaultDecoders._
import com.bharatsim.engine.graph.GraphNode
import com.bharatsim.engine.models.{Network, StatefulAgent}
import com.bharatsim.model12Hr.DayUtil.isEOD
import com.bharatsim.model12Hr.InfectionSeverity.{Mild, Severe}
import com.bharatsim.model12Hr.InfectionStatus._
import com.bharatsim.model12Hr.diseaseState._

case class Person(
    id: Long,
    infectionState: InfectionStatus,
    infectedAtTick: Int,
    isEmployee: Boolean,
    isStudent: Boolean
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

  def isPreSymptomatic: Boolean = activeState.isInstanceOf[PreSymptomaticState]

  def isMildInfected: Boolean =
    activeState.isInstanceOf[InfectedState] && activeState.asInstanceOf[InfectedState].severity == Mild

  def isSevereInfected: Boolean =
    activeState.isInstanceOf[InfectedState] && activeState.asInstanceOf[InfectedState].severity == Severe

  def isAsymptomatic: Boolean = activeState.isInstanceOf[AsymptomaticState]

  def isRecovered: Boolean = activeState.isInstanceOf[RecoveredState]

  def isDeceased: Boolean = activeState.isInstanceOf[DeceasedState]

  addRelation[House]("STAYS_AT")
  addRelation[Office]("WORKS_AT")
  addRelation[School]("STUDIES_AT")
  addRelation[Transport]("TAKES")
  addRelation[PublicPlace]("VISITS")
  addRelation[Hospital]("ADMITTED_AT")
}

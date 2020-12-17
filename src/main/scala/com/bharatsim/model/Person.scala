package com.bharatsim.model

import com.bharatsim.engine.Context
import com.bharatsim.engine.basicConversions.decoders.DefaultDecoders._
import com.bharatsim.engine.basicConversions.encoders.DefaultEncoders._
import com.bharatsim.engine.graph.GraphNode
import com.bharatsim.engine.models.{Network, StatefulAgent}
import com.bharatsim.model.InfectionStatus._
import com.bharatsim.model.diseaseState._

case class Person(
    id: Int,
    age: Int,
    infectionState: InfectionStatus,
    infectionDay: Int,
    takesPublicTransport: Boolean,
    isEssentialWorker: Boolean,
    violateLockdown: Boolean
) extends StatefulAgent {
  final val numberOfHoursInADay: Int = 24

  private def incrementInfectionDay(context: Context): Unit = {
    if ((isExposed || isInfected) && context.getCurrentStep % numberOfHoursInADay == 0) {
      updateParam("infectionDay", infectionDay + 1)
    }
  }

  def decodeNode(classType: String, node: GraphNode): Network = {
    classType match {
      case "House" => node.as[House]
      case "Office" => node.as[Office]
      case "School" => node.as[School]
      case "Transport" => node.as[Transport]
      case "PublicPlace" => node.as[PublicPlace]
    }
  }

  def isSusceptible: Boolean = activeState == SusceptibleState()

  def isExposed: Boolean = activeState == ExposedState()

  def isInfected: Boolean = activeState == InfectedState()

  def isRecovered: Boolean = activeState == RecoveredState()

  def isDeceased: Boolean = activeState == DeceasedState()

  addBehaviour(incrementInfectionDay)

  addRelation[House]("STAYS_AT")
  addRelation[Office]("WORKS_AT")
  addRelation[School]("STUDIES_AT")
  addRelation[Transport]("TAKES")
  addRelation[PublicPlace]("VISITS")
}

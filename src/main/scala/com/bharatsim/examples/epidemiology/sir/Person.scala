package com.bharatsim.examples.epidemiology.sir

import com.bharatsim.engine.Context
import com.bharatsim.engine.basicConversions.decoders.DefaultDecoders._
import com.bharatsim.engine.basicConversions.encoders.DefaultEncoders._
import com.bharatsim.engine.graph.GraphNode
import com.bharatsim.engine.models.{Agent, Node}
import com.bharatsim.engine.utils.Probability.toss
import com.bharatsim.examples.epidemiology.sir.InfectionStatus._

case class Person(id: Long, age: Int, infectionState: InfectionStatus, infectionDay: Int) extends Agent {
  final val numberOfTicksInADay: Int = 2
  private val incrementInfectionDay: Context => Unit = (context: Context) => {
    if (isInfected && context.getCurrentStep % numberOfTicksInADay == 0) {
      updateParam("infectionDay", infectionDay + 1)
    }
  }
  private val checkForInfection: Context => Unit = (context: Context) => {
    if (isSusceptible) {
      val infectionRate = Disease.beta

      val schedule = context.fetchScheduleFor(this).get

      val currentStep = context.getCurrentStep
      val placeType: String = schedule.getForStep(currentStep)

      val places = getConnections(getRelation(placeType).get).toList
      if (places.nonEmpty) {
        val place = places.head
        val decodedPlace = decodeNode(placeType, place)

        val infectedNeighbourCount = decodedPlace
          .getConnections(decodedPlace.getRelation[Person]().get)
          .count(x => x.as[Person].isInfected)

        val shouldInfect = toss(infectionRate, infectedNeighbourCount)

        if (shouldInfect) {
          updateParam("infectionState", Infected)
        }
      }
    }
  }

  private val checkForRecovery: Context => Unit = (context: Context) => {
    if (isInfected && infectionDay == Disease.lastDay
    )
      updateParam("infectionState", Removed)
  }

  def isSusceptible: Boolean = infectionState == Susceptible

  def isInfected: Boolean = infectionState == Infected

  def isRecovered: Boolean = infectionState == Removed


  private def decodeNode(classType: String, node: GraphNode): Node = {
    classType match {
      case "House" => node.as[House]
      case "Office" => node.as[Office]
      case "School" => node.as[School]
    }
  }

  addBehaviour(incrementInfectionDay)
  addBehaviour(checkForInfection)
  addBehaviour(checkForRecovery)

  addRelation[House]("STAYS_AT")
  addRelation[Office]("WORKS_AT")
  addRelation[School]("STUDIES_AT")
}

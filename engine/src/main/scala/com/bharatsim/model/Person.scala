package com.bharatsim.model

import com.bharatsim.engine.Context
import com.bharatsim.engine.basicConversions.decoders.DefaultDecoders._
import com.bharatsim.engine.basicConversions.encoders.DefaultEncoders._
import com.bharatsim.engine.models.Agent
import com.bharatsim.model.InfectionStatus._

import scala.util.Random

case class Person(id: Int, age: Int, infectionState: InfectionStatus, infectionDay: Int) extends Agent {
  final val numberOfHoursInADay: Int = 24

  private def incrementInfectionDay(context: Context): Unit = {
    if ((isExposed || isInfected) && context.simulationContext.getCurrentStep % numberOfHoursInADay == 0) {
      updateParam("infectionDay", infectionDay + 1)
    }
  }

  private def checkForExposure(context: Context): Unit = {
    if (isSusceptible) {
      val infectionRate = context.dynamics.asInstanceOf[Disease.type].infectionRate

      val schedule = context.fetchSchedules.getSchedule(this, context).get

      val currentStep = context.simulationContext.getCurrentStep
      val currentNodeType: String = schedule.getForStep(currentStep)

      val houses = context.graphProvider.fetchNeighborsOf(internalId, getRelation(currentNodeType).get)
      if (houses.nonEmpty) {
        val house = houses.head.as[House]
        val infectedNeighbourCount = context.graphProvider
          .fetchNeighborsOf(house.internalId, house.getRelation[Person]().get)
          .count(x => x.as[Person].isInfected)
        val shouldInfect = infectionRate * infectedNeighbourCount > 0

        if (shouldInfect) {
          updateParam("infectionState", Infected)
        }
      }
    }
  }

  private def checkForInfection(context: Context): Unit = {
    if (isExposed && infectionDay == context.dynamics.asInstanceOf[Disease.type].exposedDuration) {
      updateParam("infectionState", Infected)
    }
  }

  private def checkForRecovery(context: Context): Unit = {
    if (
      isInfected && infectionDay == context.dynamics
        .asInstanceOf[Disease.type]
        .lastDay
    ) {
      //      TODO: Improve the logic to evaluate based on a distribution - Jayanta
      if (Random.nextDouble() < context.dynamics.asInstanceOf[Disease.type].deathRate) {
        updateParam("infectionState", Deceased)
      } else {
        updateParam("infectionState", Recovered)
      }
    }
  }

  def isSusceptible: Boolean = infectionState == Susceptible

  def isExposed: Boolean = infectionState == Exposed

  def isInfected: Boolean = infectionState == Infected

  def isRecovered: Boolean = infectionState == Recovered

  def isDeceased: Boolean = infectionState == Deceased

  addBehaviour(incrementInfectionDay)
  addBehaviour(checkForExposure)
  addBehaviour(checkForInfection)
  addBehaviour(checkForRecovery)

  addRelation[House]("STAYS_AT")
  addRelation[Office]("WORKS_AT")
  addRelation[School]("STUDIES_AT")
}

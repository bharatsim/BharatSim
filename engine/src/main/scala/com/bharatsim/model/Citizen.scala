package com.bharatsim.model

import com.bharatsim.engine.basicConversions.decoders.DefaultDecoders._
import com.bharatsim.engine.basicConversions.encoders.DefaultEncoders._
import com.bharatsim.engine.{Agent, Context}
import com.bharatsim.model.InfectionStatus._

import scala.util.Random

case class Citizen(id: Int, age: Int, infectionState: InfectionStatus, infectionDay: Int) extends Agent {
  final val numberOfHoursInADay: Int = 24
  private val incrementInfectionDay: Context => Unit = (context: Context) => {
    if ((isExposed || isInfected) && context.simulationContext.getCurrentStep % numberOfHoursInADay == 0) {
      updateParam("infectionDay", infectionDay + 1)
    }
  }
  private val checkForExposure: Context => Unit = (context: Context) => {
    if (isSusceptible) {
      val infectionRate = context.dynamics.asInstanceOf[Disease.type].infectionRate

      val schedule = context.fetchSchedules.getSchedule(this, context).get

      val currentStep = context.simulationContext.getCurrentStep
      val currentNodeType: String = schedule.getForStep(currentStep)

      val houses = context.graphProvider.fetchNeighborsOf(internalId, getRelation(currentNodeType).get)
      if (houses.nonEmpty) {
        val house = houses.head.as[House]
        val infectedNeighbourCount = context.graphProvider.fetchNeighborsOf(house.internalId, house.getRelation[Citizen]().get)
          .count(x => x.as[Citizen].isInfected)
        val shouldInfect = infectionRate * infectedNeighbourCount > 0

        if (shouldInfect) {
          updateParam("infectionState", Infected)
        }
      }
    }
  }
  private val checkForInfection: Context => Unit = (context: Context) => {
    if (isExposed && infectionDay == context.dynamics.asInstanceOf[Disease.type].exposedDuration) {
      updateParam("infectionState", Infected)
    }
  }
  private val checkForRecovery: Context => Unit = (context: Context) => {
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

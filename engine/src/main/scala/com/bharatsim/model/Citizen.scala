package com.bharatsim.model

import com.bharatsim.engine.{Agent, Context}

import scala.util.Random

class Citizen() extends Agent {
  var infectionStatus: InfectionStatus = Susceptible
  var infectionDay: Int = 0

  def isExposed: Boolean = {
    infectionStatus == Exposed
  }

  def isInfected: Boolean = {
    infectionStatus == Infected
  }

  def isRecovered: Boolean = {
    infectionStatus == Recovered
  }

  def isDeceased: Boolean = {
    infectionStatus == Deceased
  }

  def setHome(home: House): Unit = {
    unidirectionalConnect("home", home)
  }

  def getHome(): House = {
    getConnections("home").next().asInstanceOf[House]
  }

  private val incrementInfectionDay: Context => Unit = (context: Context) => {
    if (
      (this.infectionStatus == Exposed || this.infectionStatus == Infected) && context.simulationContext.getCurrentStep % context.numberOfHoursInADay == 0
    ) {
      this.infectionDay += 1
    }
  }

  private val checkForExposure: Context => Unit = (context: Context) => {
    if (infectionStatus == Susceptible) {
      val infectionRate =
        context.dynamics.asInstanceOf[Disease.type].infectionRate

      val infectedCount = getHome().getMember().count(_.isInfected)

      val shouldInfect = Random.between(1, context.numberOfCitizen) <= infectionRate * infectedCount
      if (shouldInfect) {
        infectionStatus = Exposed
      }
    }
  }

  private val checkForInfection: Context => Unit = (context: Context) => {
    if (infectionStatus == Exposed && infectionDay == context.dynamics.asInstanceOf[Disease.type].exposedDuration) {
      infectionStatus = Infected
    }
  }

  private val checkForRecovery: Context => Unit = (context: Context) => {
    if (
      this.infectionStatus == Infected && this.infectionDay == context.dynamics
        .asInstanceOf[Disease.type]
        .lastDay
    ) {
//      TODO: Improve the logic to evaluate based on a distribution - Jayanta
      if (Random.nextDouble() < context.dynamics.asInstanceOf[Disease.type].deathRate) {
        this.infectionStatus = Deceased
      } else {
        this.infectionStatus = Recovered
      }
    }
  }

  addBehaviour(incrementInfectionDay)
  addBehaviour(checkForExposure)
  addBehaviour(checkForInfection)
  addBehaviour(checkForRecovery)
}

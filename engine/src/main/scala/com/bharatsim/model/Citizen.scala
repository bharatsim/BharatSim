package com.bharatsim.model

import com.bharatsim.engine.{Agent, Context}

import scala.util.Random

class Citizen() extends Agent {
  var infectionStatus: InfectionStatus = Susceptible
  var infectionDay: Int = 0

  def isInfected: Boolean = {
    infectionStatus == Infected
  }

  def isRecovered: Boolean = {
    infectionStatus == Recovered
  }

  def setHome(home: House): Unit = {
    unidirectionalConnect("home", home)
  }

  def getHome(): House = {
    getConnections("home").next().asInstanceOf[House]
  }

  private val incrementInfectionDay: Context => Unit = (context: Context) => {
    if (this.infectionStatus == Infected && context.simulationContext.getCurrentStep % 24 == 0) {
      this.infectionDay += 1
    }
  }

  private val checkForInfection: Context => Unit = (context: Context) => {
    if (infectionStatus == Susceptible) {
      val infectionRate =
        context.dynamics.asInstanceOf[Disease.type].infectionRate

      val infectedCount = getHome().getMember().count(_.isInfected)

      val shouldInfect = Random.between(1, 100) <= infectionRate * infectedCount
      if (shouldInfect) infectionStatus = Infected
    }
  }

  private val checkForRecovery: Context => Unit = (context: Context) => {
    if (
      this.infectionStatus == Infected && this.infectionDay == context.dynamics
        .asInstanceOf[Disease.type]
        .lastDay
    ) {
      this.infectionStatus = Recovered
    }
  }

  addBehaviour(incrementInfectionDay)
  addBehaviour(checkForInfection)
  addBehaviour(checkForRecovery)
}

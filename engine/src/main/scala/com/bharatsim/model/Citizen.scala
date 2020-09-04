package com.bharatsim.model

import com.bharatsim.engine.{Agent, Context}

import scala.util.Random

class Citizen() extends Agent {
  var infectionStatus: InfectionStatus = Susceptible

  def isInfected: Boolean = {
    infectionStatus == Infected
  }

  def setHome(home: House): Unit = {
    unidirectionalConnect("home", home)
  }

  def getHome(): House = {
    getConnections("home").next().asInstanceOf[House]
  }

  addBehaviour((context: Context) => {
    if (infectionStatus == Susceptible) {
      val infectionRate =
        context.dynamics.asInstanceOf[Disease.type].infectionRate

      val infectedCount = getHome().getMember().count(_.isInfected)

      val shouldInfect = Random.between(1, 100) <= infectionRate * infectedCount
      if (shouldInfect) infectionStatus = Infected
    }
  })
}

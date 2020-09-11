package com.bharatsim.model

import com.bharatsim.engine.{Agent, Context}

import scala.util.Random

class Citizen() extends Agent {
  def isInfected: Boolean = {
    fetchParam("infectionStatus") match {
      case Some(value) => value == Infected
      case None => false
    }
  }

  def setHome(home: House): Unit = {
    unidirectionalConnect("STAYS_AT", home)
  }

  def getHome: House = {
    getConnections("STAYS_AT").next().asInstanceOf[House]
  }

  addBehaviour((context: Context) => {
    if (isInfected) {
      val infectionRate = context.dynamics.asInstanceOf[Disease.type].infectionRate

      val home = getHome
      val infectedCount = context.graphProvider.fetchNeighborsOf(home.id, "MEMBER_OF").count(x => x("infectionState").get == Infected)

      val shouldInfect = Random.between(1, 100) <= infectionRate * infectedCount
      if (shouldInfect) {
        updateParam("infectionStatus", Infected)
      }
    }
  })
}

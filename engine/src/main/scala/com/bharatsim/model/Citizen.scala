package com.bharatsim.model

import com.bharatsim.engine.{Agent, Context, Node}
import InfectionStatus._
import scala.util.Random

class Citizen() extends Agent {
  def isInfected: Boolean = {
    fetchParam("infectionState") match {
      case Some(value) => value == Infected
      case None        => false
    }
  }

  def setHome(home: House): Unit = {
    unidirectionalConnect("STAYS_AT", home)
  }

  def getHome: House = {
    getConnections[House]("STAYS_AT").next()
  }

  addBehaviour((context: Context) => {
    if (!isInfected) {
      val infectionRate = context.dynamics.asInstanceOf[Disease.type].infectionRate
      val infected = fetchParam("infectionState").get == Infected

      val home = getHome
      val infectedCount =
        context.graphProvider.fetchNeighborsOf(home.id, "HOUSES").count(x => x("infectionState").get == Infected)
      val shouldInfect = Random.between(1, 100) <= infectionRate * infectedCount
      if (shouldInfect) {
        updateParam("infectionState", Infected)
      }
    }
  })
}

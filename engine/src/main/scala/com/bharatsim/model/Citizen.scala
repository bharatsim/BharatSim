package com.bharatsim.model

import com.bharatsim.engine.{Agent, Context}
import com.bharatsim.model.InfectionStatus._

import scala.util.Random

class Citizen() extends Agent {

  final val numberOfHoursInADay: Int = 24

  def isSusceptible: Boolean = {
    fetchParam("infectionState") match {
      case Some(value) => value == Susceptible
      case None        => false
    }
  }

  def isExposed: Boolean = {
    fetchParam("infectionState") match {
      case Some(value) => value == Exposed
      case None        => false
    }
  }

  def isInfected: Boolean = {
    fetchParam("infectionState") match {
      case Some(value) => value == Infected
      case None        => false
    }
  }

  def isRecovered: Boolean = {
    fetchParam("infectionState") match {
      case Some(value) => value == Recovered
      case None        => false
    }
  }

  def isDeceased: Boolean = {
    fetchParam("infectionState") match {
      case Some(value) => value == Deceased
      case None        => false
    }
  }

  def getInfectionDay: Int = {
    fetchParam("infectionDay") match {
      case Some(value: Int) => value
      case None             => 0
    }
  }

  def getAge: Int = {
    fetchParam("age").get.asInstanceOf[Int]
  }

  def setHome(home: House): Unit = {
    unidirectionalConnect("STAYS_AT", home)
  }

  def getHome: House = {
    getConnections[House]("STAYS_AT").next()
  }

  private val incrementInfectionDay: Context => Unit = (context: Context) => {
    if (
      (isExposed || isInfected) &&
      context.simulationContext.getCurrentStep % numberOfHoursInADay == 0
    ) {
      fetchParam("infectionDay") match {
        case Some(value: Int) => updateParam("infectionDay", value + 1)
        case None             => updateParam("infectionDay", 1)
      }
    }
  }

  private val checkForExposure: Context => Unit = (context: Context) => {
    if (isSusceptible) {
      val infectionRate = context.dynamics.asInstanceOf[Disease.type].infectionRate

      val schedule = context.schedules.getSchedule(this, context).get
      val currentPlace = schedule.getForStep(context.simulationContext.getCurrentStep);

      val home = getHome
      val infectedCount =
        context.graphProvider.fetchNeighborsOf(home.id, "HOUSES").count(x => x("infectionState").get == Infected)

      val shouldInfect = infectionRate * infectedCount > 0

      if (shouldInfect) {
        updateParam("infectionState", Exposed)
      }
    }
  }

  private val checkForInfection: Context => Unit = (context: Context) => {
    if (isExposed && getInfectionDay == context.dynamics.asInstanceOf[Disease.type].exposedDuration) {
      updateParam("infectionState", Infected)
    }
  }

  private val checkForRecovery: Context => Unit = (context: Context) => {
    if (
      isInfected && getInfectionDay == context.dynamics
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

  addBehaviour(incrementInfectionDay)
  addBehaviour(checkForExposure)
  addBehaviour(checkForInfection)
  addBehaviour(checkForRecovery)
}

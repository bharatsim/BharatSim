package com.bharatsim.model.diseaseState

import com.bharatsim.engine.Context
import com.bharatsim.engine.fsm.State
import com.bharatsim.engine.models.StatefulAgent
import com.bharatsim.model.{Disease, Person}
import com.bharatsim.model.InfectionStatus.{Deceased, Recovered, Susceptible}
import com.bharatsim.engine.basicConversions.decoders.DefaultDecoders._
import com.bharatsim.engine.basicConversions.encoders.DefaultEncoders._

import scala.util.Random

case class InfectedState() extends State {

  private def checkInfectionLastDay(context: Context, agent: StatefulAgent):Boolean = {
    agent.asInstanceOf[Person].isInfected && agent.asInstanceOf[Person].infectionDay == context.dynamics
      .asInstanceOf[Disease.type]
      .lastDay
  }

  def checkForRecovery(context: Context, agent: StatefulAgent): Boolean = {
    if (checkInfectionLastDay(context, agent)) {
      //      TODO: Improve the logic to evaluate based on a distribution - Jayanta
      if (Random.nextDouble() >= context.dynamics.asInstanceOf[Disease.type].deathRate) {
        agent.updateParam("infectionState", Recovered)
        return true
      }
    }
    false
  }

  def checkForDeceased(context: Context, agent: StatefulAgent): Boolean = {
    if (checkInfectionLastDay(context, agent)){
      //      TODO: Improve the logic to evaluate based on a distribution - Jayanta
      if (Random.nextDouble() < context.dynamics.asInstanceOf[Disease.type].deathRate) {
        agent.updateParam("infectionState", Deceased)
        println("Moving to Deceased")
        return true
      }
    }
    return false
  }

  addTransition(
    when = checkForRecovery,
    to = context => RecoveredState()
  )

  addTransition(
    when = checkForDeceased,
    to = context => DeceasedState()
  )
}

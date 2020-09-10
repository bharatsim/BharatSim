package com.bharatsim.model
import com.bharatsim.engine.{Context, Simulation}

import scala.util.Random

object Main {
  def setUpContext(context: Context): Unit = {

    val numberOfCitizen = 10
    val house = new House

    for (_ <- 1 to numberOfCitizen) {
      val citizen = new Citizen()
      citizen.infectionStatus = if (Random.nextBoolean()) Infected else Susceptible
      context.agents.add(citizen)
      house.addMember(citizen)
    }
    context.simulationContext.setSteps(100)
    context.setDynamics(Disease)
  }

  def main(args: Array[String]): Unit = {
    val context = new Context
    setUpContext(context)
    val totalInfectedBeforeSimulation =
      context.agents.getAll.count(_.asInstanceOf[Citizen].isInfected)
    Simulation.run(context)

    val totalRecovered =
      context.agents.getAll.count(_.asInstanceOf[Citizen].isRecovered)
    println("#Infected before simulation: " + totalInfectedBeforeSimulation)

    println("#Recovered after simulation: " + totalRecovered)
  }

}

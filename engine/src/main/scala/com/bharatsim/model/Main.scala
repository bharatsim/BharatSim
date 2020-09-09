package com.bharatsim.model
import com.bharatsim.engine.{Context, Simulation}

import scala.util.Random

object Main {
  def steUpContext(context: Context): Unit = {

    val numberOfCitizen = 10
    val house = new House

    for (i <- 1 to numberOfCitizen) {
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
    steUpContext(context)
    val totalInfectedBeforeSimulation =
      context.agents.getAll.count(_.asInstanceOf[Citizen].isInfected)
    Simulation.run(context)

    val totalRecovered =
      context.agents.getAll.count(_.asInstanceOf[Citizen].isRecovered)
    println("#Infected before simulation: " + totalInfectedBeforeSimulation)

    println("#Recovered after simulation: " + totalRecovered)
  }

}

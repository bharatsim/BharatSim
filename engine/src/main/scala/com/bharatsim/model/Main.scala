package com.bharatsim.model
import com.bharatsim.engine.{Context, Simulation}

import scala.util.Random

object Main {
  def setUpContext(context: Context): Unit = {

    val house = new House

    for (_ <- 1 to context.numberOfCitizen) {
      val citizen = new Citizen()
      citizen.infectionStatus = if (Random.nextBoolean()) Infected else Susceptible
      context.agents.add(citizen)
      house.addMember(citizen)
    }
    context.simulationContext.setSteps(1000)
    context.setDynamics(Disease)
  }

  def main(args: Array[String]): Unit = {
    val context = new Context
    setUpContext(context)
    val totalInfectedBeforeSimulation =
      context.agents.getAll.count(_.asInstanceOf[Citizen].isInfected)

    Simulation.run(context)

    val totalInfectedAfterSimulation =
      context.agents.getAll.count(_.asInstanceOf[Citizen].isInfected)
    val totalExposed =
      context.agents.getAll.count(_.asInstanceOf[Citizen].isExposed)
    val totalRecovered =
      context.agents.getAll.count(_.asInstanceOf[Citizen].isRecovered)
    val totalDeceased =
      context.agents.getAll.count(_.asInstanceOf[Citizen].isDeceased)

    println("#Infected before simulation: " + totalInfectedBeforeSimulation)
    println("#Infected after simulation: " + totalInfectedAfterSimulation)
    println("#Exposed after simulation: " + totalExposed)
    println("#Recovered after simulation: " + totalRecovered)
    println("#Deceased after simulation: " + totalDeceased)
  }

}

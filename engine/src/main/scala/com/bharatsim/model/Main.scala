package com.bharatsim.model

import com.bharatsim.engine.{Context, Network, Simulation}

import scala.util.Random

object Main {
  def steUpContext(context: Context): Unit = {
    val numberOfCitizen = 10
    val house = new Network
    context.networks.add(house)
    for (_ <- 1 to numberOfCitizen) {
      val citizen = new Citizen
      house.connectAgent(citizen)
      citizen.infectionStatus =
        if (Random.nextBoolean()) Infected else Susceptible
      context.agents.add(citizen)
    }
    context.simulationContext.setSteps(10)
    context.setDynamics(Disease)
  }

  def main(args: Array[String]): Unit = {
    val context = new Context
    steUpContext(context)
    val beforeCount =
      context.agents.getAll.count(_.asInstanceOf[Citizen].isInfected)
    Simulation.run(context)
    val afterCount =
      context.agents.getAll.count(_.asInstanceOf[Citizen].isInfected)
    println(beforeCount)
    println(afterCount)
  }

}

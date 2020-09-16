package com.bharatsim.model
import com.bharatsim.engine.{Context, Simulation, SimulationContext}

import scala.util.Random

object Main {
  def main(args: Array[String]): Unit = {
    val simulationContext = new SimulationContext(10)
    implicit val context: Context = Context(Disease, simulationContext)

    ingestData()
    val beforeCount = context.graphProvider.fetchNodes("Citizen", ("infectionState", Infected)).size
    context.registerAgent[Citizen]

    Simulation.run(context)

    val afterCount = context.graphProvider.fetchNodes("Citizen", ("infectionState", Infected)).size

    println(beforeCount)
    println(afterCount)
  }

  private def ingestData()(implicit context: Context): Unit = {
    val numberOfCitizen = 100
    val houseId = context.graphProvider.createNode("Home")

    for (i <- 1 to numberOfCitizen) {
      val infectionState = if (Random.nextBoolean()) Infected else Susceptible
      val agentId = context.graphProvider.createNode("Citizen", ("infectionState", infectionState))
      context.graphProvider.createRelationship(agentId, "STAYS_AT", houseId)
      context.graphProvider.createRelationship(houseId, "MEMBER_OF", agentId)
    }
  }
}

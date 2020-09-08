package com.bharatsim.model
import com.bharatsim.engine.graph.GraphProviderImpl
import com.bharatsim.engine.{Context, Simulation, SimulationContext}

import scala.util.Random

object Main {
  def main(args: Array[String]): Unit = {
    val graphProvider = new GraphProviderImpl
    val simulationContext = new SimulationContext(10)
    implicit val context: Context = new Context(graphProvider, Disease, simulationContext)

    ingestData()
    val beforeCount = context.graphProvider.fetchNodes("Citizens", ("infectionState", Infected)).size

    Simulation.run(context)

    val afterCount = context.graphProvider.fetchNodes("Citizens", ("infectionState", Infected)).size

    println(beforeCount)
    println(afterCount)
  }

  private def ingestData()(implicit context: Context): Unit = {
    val numberOfCitizen = 10
    val houseId = context.graphProvider.createNode("Home")

    for (i <- 1 to numberOfCitizen) {
      val infectionState = if (Random.nextBoolean()) Infected else Susceptible
      val agentId = context.graphProvider.createNode("Citizen", ("infectionState", infectionState))
      context.graphProvider.createRelationship("STAYS_AT", agentId, houseId)
      context.graphProvider.createRelationship("MEMBER_OF", houseId, agentId)
    }
  }
}

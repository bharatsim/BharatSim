package com.bharatsim.model
import com.bharatsim.engine.graph.{DataNode, GraphData, Relation}
import com.bharatsim.engine.graph.GraphProvider.NodeId
import com.bharatsim.engine.{Context, Simulation, SimulationContext}
import com.bharatsim.model.InfectionStatus._

import scala.collection.mutable
import scala.util.Random

object Main {
  def main(args: Array[String]): Unit = {
    val simulationContext = new SimulationContext(1000)
    implicit val context: Context = Context(Disease, simulationContext)

    ingestDataUsingCSV(context)

//    ingestData()
    val beforeCount = context.graphProvider.fetchNodes("Citizen", ("infectionState", Infected)).size
    context.registerAgent[Citizen]

    Simulation.run(context)

    val afterCountSusceptible = context.graphProvider.fetchNodes("Citizen", ("infectionState", Susceptible)).size
    val afterCountInfected = context.graphProvider.fetchNodes("Citizen", ("infectionState", Infected)).size
    val afterCountRecovered = context.graphProvider.fetchNodes("Citizen", ("infectionState", Recovered)).size
    val afterCountDeceased = context.graphProvider.fetchNodes("Citizen", ("infectionState", Deceased)).size

    println("Infected before: " + beforeCount)
    println("Infected after: " + afterCountInfected)
    println("Recovered: " + afterCountRecovered)
    println("Deceased: " + afterCountDeceased)
    println("Susceptible: " + afterCountSusceptible)
  }

  private def ingestDataUsingCSV(context: Context): Unit = {
    context.graphProvider.ingestFromCsv(
      "src/main/resources/citizen.csv",
      Some(value = (map: Map[String, String]) => {
        val params = new mutable.HashMap[String, Any]
        params.put("infectionState", InfectionStatus.withName(map("infectionState")))
        params.put("age", map("age").toInt)
        val citizenNode = new DataNode {
          override def label: String = map("label")

          override def Id: NodeId = map("id").toInt

          override def getParams: Map[String, Any] = params.toMap
        }
        val homeNode = new DataNode {
          override def label: String = "House";

          override def Id: NodeId = map("house_id").toInt

          override def getParams: Map[String, Any] = Map.empty
        }
        val staysAt = Relation(citizenNode, "STAYS_AT", homeNode)
        val memberOf = Relation(homeNode, "HOUSES", citizenNode)
        GraphData(List(citizenNode, homeNode), List(staysAt, memberOf))
      })
    )
  }

  private def ingestData()(implicit context: Context): Unit = {
    val numberOfCitizen = 100
    val houseId = context.graphProvider.createNode("Home")

    for (_ <- 1 to numberOfCitizen) {
      val infectionState = if (Random.nextBoolean()) Infected else Susceptible
      val agentId = context.graphProvider.createNode("Citizen", ("infectionState", infectionState))
      context.graphProvider.createRelationship(agentId, "STAYS_AT", houseId)
      context.graphProvider.createRelationship(houseId, "MEMBER_OF", agentId)
    }
  }
}

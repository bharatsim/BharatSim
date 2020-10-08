package com.bharatsim.model

import com.bharatsim.engine._
import com.bharatsim.engine.basicConversions.decoders.DefaultDecoders._
import com.bharatsim.engine.basicConversions.encoders.DefaultEncoders._
import com.bharatsim.engine.graph.{GraphData, Relation}

object Main {
  def main(args: Array[String]): Unit = {
    val simulationContext = new SimulationContext(1000)
    implicit val context: Context = Context(Disease, simulationContext)

    val employeeScheduleOnWeekDays = new Schedule(Day, Hour)
      .add("Home", 0, 8)
      .add("Home", 19, 23)

    val employeeScheduleOnWeekEnd = new Schedule(Day, Hour)
      .add("Home", 0, 23)

    val employeeSchedule = new Schedule(Week, Day)
      .add(employeeScheduleOnWeekDays, 0, 4)
      .add(employeeScheduleOnWeekEnd, 5, 6)

    val studentSchedule = new Schedule(Day, Hour)
      .add("Home", 0, 8)
      .add("School", 9, 14)
      .add("Daycare", 15, 18)
      .add("Home", 19, 23)

    context.schedules.addSchedule(
      employeeSchedule,
      (agent: Agent, context: Context) => agent.asInstanceOf[Citizen2].age >= 30
    )
    context.schedules.addSchedule(
      studentSchedule,
      (agent: Agent, context: Context) => agent.asInstanceOf[Citizen2].age < 30
    )

    ingestDataUsingCSV(context)

    //    ingestData()
    val beforeCount = context.graphProvider.fetchNodes("Citizen2", ("infectionState", "Infected")).size
    context.registerAgent[Citizen2]

    Simulation.run(context)

    val afterCountSusceptible = context.graphProvider.fetchNodes("Citizen2", ("infectionState", "Susceptible")).size
    val afterCountInfected = context.graphProvider.fetchNodes("Citizen2", ("infectionState", "Infected")).size
    val afterCountRecovered = context.graphProvider.fetchNodes("Citizen2", ("infectionState", "Recovered")).size
    val afterCountDeceased = context.graphProvider.fetchNodes("Citizen2", ("infectionState", "Deceased")).size

    println("Infected before: " + beforeCount)
    println("Infected after: " + afterCountInfected)
    println("Recovered: " + afterCountRecovered)
    println("Deceased: " + afterCountDeceased)
    println("Susceptible: " + afterCountSusceptible)
  }

  private def ingestDataUsingCSV(context: Context): Unit = {
    context.graphProvider.ingestFromCsv(
      "src/main/resources/citizen.csv",
      Some((map: Map[String, String]) => {
        val age = map("age").toInt
        val citizen: Citizen2 = Citizen2(age, InfectionStatus.withName(map("infectionState")), 0)
        val home = House2()

        val citizenId = map("id").toInt
        val homeId = map("house_id").toInt

        val staysAt = Relation(citizenId, "STAYS_AT", homeId)
        val memberOf = Relation(homeId, "HOUSES", citizenId)

        val graphData = new GraphData()
        graphData.addNode(citizenId, citizen)
        graphData.addNode(homeId, home)
        graphData.addRelations(List(staysAt, memberOf))
        graphData
      })
    )
  }
}

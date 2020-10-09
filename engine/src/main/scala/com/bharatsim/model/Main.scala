package com.bharatsim.model

import com.bharatsim.engine.ContextBuilder._
import com.bharatsim.engine._
import com.bharatsim.engine.basicConversions.decoders.DefaultDecoders._
import com.bharatsim.engine.basicConversions.encoders.DefaultEncoders._
import com.bharatsim.engine.dsl.SyntaxHelpers._
import com.bharatsim.engine.graph.{GraphData, Relation}
import com.bharatsim.engine.listners.{CsvOutputGenerator, SimulationListenerRegistry}
import com.typesafe.scalalogging.LazyLogging

object Main extends LazyLogging {
  def main(args: Array[String]): Unit = {
    val simulationContext = new SimulationContext(1000)
    implicit val context: Context = Context(Disease, simulationContext)

    createSchedules()

    ingestCSVData("src/main/resources/citizen.csv", csvDataExtractor)
    val beforeCount = context.graphProvider.fetchNodes("Citizen", ("infectionState", "Infected")).size

    context.registerAgent[Citizen]

    SimulationListenerRegistry.register(
      new CsvOutputGenerator("src/main/resources/output.csv", new SEIROutputSpec(context))
    );

    Simulation.run(context)

    printStats(beforeCount)
  }

  private def createSchedules()(implicit context: Context): Unit = {
    val employeeScheduleOnWeekDays = (Day, Hour)
      .add("Home", 0, 8)
      .add("Home", 19, 23)

    val employeeScheduleOnWeekEnd = (Day, Hour).add("Home", 0, 23)

    val employeeSchedule = (Week, Day)
      .add(employeeScheduleOnWeekDays, 0, 4)
      .add(employeeScheduleOnWeekEnd, 5, 6)

    val studentSchedule = (Day, Hour)
      .add("Home", 0, 8)
      .add("School", 9, 14)
      .add("Daycare", 15, 18)
      .add("Home", 19, 23)

    registerSchedules(
      (employeeSchedule, (agent: Agent, _: Context) => agent.asInstanceOf[Citizen].age >= 30),
      (studentSchedule, (agent: Agent, _: Context) => agent.asInstanceOf[Citizen].age < 30)
    )
  }

  private def csvDataExtractor(map: Map[String, String]): GraphData = {

    val citizenId = map("id").toInt
    val age = map("age").toInt
    val citizen: Citizen = Citizen(citizenId, age, InfectionStatus.withName(map("infectionState")), 0)

    val homeId = map("house_id").toInt
    val home = House(homeId)

    val staysAt = Relation(citizenId, "STAYS_AT", homeId)
    val memberOf = Relation(homeId, "HOUSES", citizenId)

    val graphData = new GraphData()
    graphData.addNode(citizenId, citizen)
    graphData.addNode(homeId, home)
    graphData.addRelations(List(staysAt, memberOf))
    graphData
  }

  private def printStats(beforeCount: Int)(implicit context: Context): Unit = {
    val afterCountSusceptible = context.graphProvider.fetchNodes("Citizen", ("infectionState", "Susceptible")).size
    val afterCountInfected = context.graphProvider.fetchNodes("Citizen", ("infectionState", "Infected")).size
    val afterCountRecovered = context.graphProvider.fetchNodes("Citizen", ("infectionState", "Recovered")).size
    val afterCountDeceased = context.graphProvider.fetchNodes("Citizen", ("infectionState", "Deceased")).size

    logger.info("Infected before: {}", beforeCount)
    logger.info("Infected after: {}", afterCountInfected)
    logger.info("Recovered: {}", afterCountRecovered)
    logger.info("Deceased: {}", afterCountDeceased)
    logger.info("Susceptible: {}", afterCountSusceptible)
  }
}

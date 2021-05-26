package com.bharatsim.examples.epidemiology.sir

import java.util.Date

import com.bharatsim.engine.ContextBuilder._
import com.bharatsim.engine._
import com.bharatsim.engine.actions.StopSimulation
import com.bharatsim.engine.basicConversions.decoders.DefaultDecoders._
import com.bharatsim.engine.basicConversions.encoders.DefaultEncoders._
import com.bharatsim.engine.dsl.SyntaxHelpers._
import com.bharatsim.engine.execution.Simulation
import com.bharatsim.engine.graph.ingestion.{GraphData, Relation}
import com.bharatsim.engine.graph.patternMatcher.MatchCondition._
import com.bharatsim.engine.listeners.{CsvOutputGenerator, SimulationListenerRegistry}
import com.bharatsim.engine.models.Agent
import com.bharatsim.engine.utils.Probability.biasedCoinToss
import com.bharatsim.examples.epidemiology.sir.InfectionStatus._
import com.bharatsim.model.Disease
import com.typesafe.scalalogging.LazyLogging

object Main extends LazyLogging {
  private val initialInfectedFraction = 0.01

  private val myTick: ScheduleUnit = new ScheduleUnit(1)
  private val myDay: ScheduleUnit = new ScheduleUnit(myTick * 2)

  def main(args: Array[String]): Unit = {
    var beforeCount = 0
    val simulation = Simulation()

    simulation.ingestData(implicit context => {
      ingestCSVData("citizen10k.csv", csvDataExtractor)
      logger.debug("Ingestion done")
    })

    simulation.defineSimulation(implicit context => {
      create12HourSchedules()

      registerAction(
        StopSimulation,
        (c: Context) => {
          getInfectedCount(c) == 0
        }
      )

      beforeCount = getInfectedCount(context)

      registerAgent[Person]

      val currentTime = new Date().getTime

      SimulationListenerRegistry.register(
        new CsvOutputGenerator("src/main/resources/output_" + currentTime + ".csv", new SEIROutputSpec(context))
      )
    })

    simulation.onCompleteSimulation { implicit context =>
      printStats(beforeCount)
      teardown()
    }

    val startTime = System.currentTimeMillis()
    simulation.run()
    val endTime = System.currentTimeMillis()
    logger.info("Total time: {} s", (endTime - startTime) / 1000)
  }

  private def create12HourSchedules()(implicit context: Context): Unit = {
    val employeeSchedule = (myDay, myTick)
      .add[House](0, 0)
      .add[Office](1, 1)

    val studentSchedule = (myDay, myTick)
      .add[House](0, 0)
      .add[School](1, 1)

    val quarantinedSchedule = (Day, Hour)
      .add[House](0, 1)

    registerSchedules(
      (quarantinedSchedule, (agent: Agent, _: Context) => agent.asInstanceOf[Person].isInfected, 1),
      (employeeSchedule, (agent: Agent, _: Context) => agent.asInstanceOf[Person].age >= 25, 2),
      (studentSchedule, (agent: Agent, _: Context) => agent.asInstanceOf[Person].age < 25, 3)
    )
  }

  private def csvDataExtractor(map: Map[String, String])(implicit context: Context): GraphData = {

    val citizenId = map("Agent_ID").toLong
    val age = map("Age").toInt
    val initialInfectionState = if (biasedCoinToss(initialInfectedFraction)) "Infected" else "Susceptible"

    val homeId = map("HHID").toLong
    val schoolId = map("school_id").toLong
    val officeId = map("WorkPlaceID").toLong

    val citizen: Person = Person(
      citizenId,
      age,
      InfectionStatus.withName(initialInfectionState),
      0
    )

    val home = House(homeId)
    val staysAt = Relation[Person, House](citizenId, "STAYS_AT", homeId)
    val memberOf = Relation[House, Person](homeId, "HOUSES", citizenId)

    val graphData = GraphData()
    graphData.addNode(citizenId, citizen)
    graphData.addNode(homeId, home)
    graphData.addRelations(staysAt, memberOf)

    if (age >= 25) {
      val office = Office(officeId)
      val worksAt = Relation[Person, Office](citizenId, "WORKS_AT", officeId)
      val employerOf = Relation[Office, Person](officeId, "EMPLOYER_OF", citizenId)

      graphData.addNode(officeId, office)
      graphData.addRelations(worksAt, employerOf)
    } else {
      val school = School(schoolId)
      val studiesAt = Relation[Person, School](citizenId, "STUDIES_AT", schoolId)
      val studentOf = Relation[School, Person](schoolId, "STUDENT_OF", citizenId)

      graphData.addNode(schoolId, school)
      graphData.addRelations(studiesAt, studentOf)
    }

    graphData
  }

  private def printStats(beforeCount: Int)(implicit context: Context): Unit = {
    val afterCountSusceptible = getSusceptibleCount(context)
    val afterCountInfected = getInfectedCount(context)
    val afterCountRecovered = getRemovedCount(context)

    logger.info("Infected before: {}", beforeCount)
    logger.info("Infected after: {}", afterCountInfected)
    logger.info("Recovered: {}", afterCountRecovered)
    logger.info("Susceptible: {}", afterCountSusceptible)
  }

  private def getSusceptibleCount(context: Context) = {
    context.graphProvider.fetchCount("Person", "infectionState" equ Susceptible)
  }

  private def getInfectedCount(context: Context): Int = {
    context.graphProvider.fetchCount("Person", ("infectionState" equ Infected))
  }

  private def getRemovedCount(context: Context) = {
    context.graphProvider.fetchCount("Person", "infectionState" equ Removed)
  }
}

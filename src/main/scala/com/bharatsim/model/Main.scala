package com.bharatsim.model

import com.bharatsim.engine.ContextBuilder._
import com.bharatsim.engine._
import com.bharatsim.engine.actions.StopSimulation
import com.bharatsim.engine.basicConversions.decoders.DefaultDecoders._
import com.bharatsim.engine.basicConversions.encoders.DefaultEncoders._
import com.bharatsim.engine.dsl.SyntaxHelpers._
import com.bharatsim.engine.graph.patternMatcher.MatchCondition._
import com.bharatsim.engine.graph.{GraphData, Relation}
import com.bharatsim.engine.listeners.{CsvOutputGenerator, SimulationListenerRegistry}
import com.bharatsim.engine.models.Agent
import com.bharatsim.model.InfectionStatus._
import com.typesafe.scalalogging.LazyLogging

object Main extends LazyLogging {
  def main(args: Array[String]): Unit = {
    val config = SimulationConfig(5000)
    implicit val context: Context = Context(Disease, config)

    createSchedules()

    registerAction(
      StopSimulation,
      (c: Context) => {
        getInfectedCount(c) == 0 && getExposedCount(c) == 0
      }
    )

    ingestCSVData("src/main/resources/citizen.csv", csvDataExtractor)
    logger.debug("Ingestion done")
    val beforeCount = getInfectedCount(context)

    registerAgent[Person]

    SimulationListenerRegistry.register(
      new CsvOutputGenerator("src/main/resources/output.csv", new SEIROutputSpec(context))
    )

    Simulation.run()

    printStats(beforeCount)

    teardown()
  }

  private def createSchedules()(implicit context: Context): Unit = {
    val employeeScheduleOnWeekDays = (Day, Hour)
      .add[House](0, 8)
      .add[Office](9, 18)
      .add[House](19, 23)

    val employeeScheduleOnWeekEnd = (Day, Hour).add[House](0, 23)

    val employeeSchedule = (Week, Day)
      .add(employeeScheduleOnWeekDays, 0, 4)
      .add(employeeScheduleOnWeekEnd, 5, 6)

    val employeeScheduleWithPublicTransport = (Day, Hour)
      .add[House](0, 8)
      .add[Transport](9, 10)
      .add[Office](11, 18)
      .add[Transport](19, 20)
      .add[House](21, 23)

    val studentScheduleOnWeekDay = (Day, Hour)
      .add[House](0, 8)
      .add[School](9, 15)
      .add[House](16, 23)

    val studentScheduleOnWeekEnd = (Day, Hour).add[House](0, 23)

    val studentSchedule = (Week, Day)
      .add(studentScheduleOnWeekDay, 0, 4)
      .add(studentScheduleOnWeekEnd, 5, 6)

    registerSchedules(
      (
        employeeScheduleWithPublicTransport,
        (agent: Agent, _: Context) =>
          agent.asInstanceOf[Person].takesPublicTransport && agent.asInstanceOf[Person].age >= 30
      ),
      (employeeSchedule, (agent: Agent, _: Context) => agent.asInstanceOf[Person].age >= 30),
      (studentSchedule, (agent: Agent, _: Context) => agent.asInstanceOf[Person].age < 30)
    )
  }

  private def csvDataExtractor(map: Map[String, String]): GraphData = {

    val citizenId = map("id").toInt
    val age = map("age").toInt
    val takesPublicTransport = map("public_transport").toBoolean

    val citizen: Person =
      Person(citizenId, age, InfectionStatus.withName(map("infectionState")), 0, takesPublicTransport)

    val homeId = map("house_id").toInt
    val officeId = map("office_id").toInt
    val schoolId = map("school_id").toInt

    val home = House(homeId)
    val office = Office(officeId)
    val school = School(schoolId)

    val staysAt = Relation[Person, House](citizenId, "STAYS_AT", homeId)
    val worksAt = Relation[Person, Office](citizenId, "WORKS_AT", officeId)
    val studiesAt = Relation[Person, School](citizenId, "STUDIES_AT", schoolId)

    val memberOf = Relation[House, Person](homeId, "HOUSES", citizenId)
    val employerOf = Relation[Office, Person](officeId, "EMPLOYER_OF", citizenId)
    val studentOf = Relation[School, Person](schoolId, "STUDENT_OF", citizenId)

    val graphData = new GraphData()
    graphData.addNode(citizenId, citizen)
    graphData.addNode(homeId, home)
    graphData.addNode(officeId, office)
    graphData.addNode(schoolId, school)

    graphData.addRelations(List(staysAt, worksAt, studiesAt, memberOf, employerOf, studentOf))

    if (takesPublicTransport) {
      val transportId = 1;
      val transport = Transport(transportId);
      val takes = Relation[Person, Transport](citizenId, citizen.getRelation[Transport]().get, transportId)
      val carries = Relation[Transport, Person](transportId, transport.getRelation[Person]().get, citizenId)
      graphData.addNode(transportId, transport)
      graphData.addRelations(List(takes, carries))
    }

    graphData
  }

  private def printStats(beforeCount: Int)(implicit context: Context): Unit = {
    val afterCountSusceptible = getSusceptibleCount(context)
    val afterCountInfected = getInfectedCount(context)
    val afterCountRecovered = context.graphProvider.fetchCount("Person", "infectionState" equ Recovered)
    val afterCountDeceased = context.graphProvider.fetchCount("Person", "infectionState" equ Deceased)

    logger.info("Infected before: {}", beforeCount)
    logger.info("Infected after: {}", afterCountInfected)
    logger.info("Recovered: {}", afterCountRecovered)
    logger.info("Deceased: {}", afterCountDeceased)
    logger.info("Exposed: {}", getExposedCount(context))
    logger.info("Susceptible: {}", afterCountSusceptible)
  }

  private def getSusceptibleCount(context: Context) = {
    context.graphProvider.fetchCount("Person", "infectionState" equ Susceptible)
  }

  private def getExposedCount(context: Context) = {
    context.graphProvider.fetchCount("Person", "infectionState" equ Exposed)
  }

  private def getInfectedCount(context: Context): Int = {
    context.graphProvider.fetchCount("Person", "infectionState" equ Infected)
  }
}

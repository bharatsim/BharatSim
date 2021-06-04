package com.bharatsim.model12Hr

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
import com.bharatsim.model12Hr.InfectionSeverity.{Mild, Severe}
import com.bharatsim.model12Hr.InfectionStatus._
import com.bharatsim.model12Hr.diseaseState._
import com.typesafe.scalalogging.LazyLogging

object Main2 extends LazyLogging {
  private val TOTAL_PUBLIC_PLACES = 100
  private var lastPublicPlaceId = 1
  private val initialInfectedFraction = 0.04

  val myTick: ScheduleUnit = new ScheduleUnit(1)
  val myDay: ScheduleUnit = new ScheduleUnit(myTick * 2)

  def main(args: Array[String]): Unit = {
    var beforeCount = 0
    val distributedSimulation = Simulation()

    distributedSimulation.ingestData { implicit context =>
      ingestCSVData("local/input.csv", csvDataExtractor)
    }

    distributedSimulation.defineSimulation { implicit context =>
      createBasicSchedule()

      registerAction(
        StopSimulation,
        (c: Context) => {
          getInfectedCount(c) == 0 && getExposedCount(c) == 0
        }
      )

      beforeCount = getInfectedCount(context)

      registerAgent[Person]

      registerState[AsymptomaticState]
      registerState[DeceasedState]
      registerState[ExposedState]
      registerState[InfectedState]
      registerState[PreSymptomaticState]
      registerState[RecoveredState]
      registerState[SusceptibleState]

      SimulationListenerRegistry.register(
        new CsvOutputGenerator("src/main/resources/12hr_model_output.csv", new SEIROutputSpec(context))
      )
    }

    distributedSimulation.onCompleteSimulation { implicit context =>
      printStats(beforeCount)
      teardown()
    }

    distributedSimulation.run()
  }

  private def createBasicSchedule()(implicit context: Context): Unit = {
    val employeeSchedule = (myDay, myTick)
      .add[House](0, 0)
      .add[Office](1, 1)

    val studentSchedule = (myDay, myTick)
      .add[House](0, 0)
      .add[School](1, 1)

    registerSchedules(
      (employeeSchedule, (agent: Agent, _: Context) => agent.asInstanceOf[Person].isEmployee, 3),
      (studentSchedule, (agent: Agent, _: Context) => agent.asInstanceOf[Person].isStudent, 4)
    )
  }

  private def csvDataExtractor(map: Map[String, String])(implicit context: Context): GraphData = {

    val citizenId = map("Agent_ID").toLong

    val initialInfectionState = if (biasedCoinToss(initialInfectedFraction)) "InfectedMild" else "Susceptible"

    val homeId = map("HHID").toLong
    val officeId = map("WorkPlaceID").toLong
    val schoolId = map("school_id").toLong
    val publicPlaceId = generatePublicPlaceId()

    val isEmployee: Boolean = officeId > 0
    val isStudent: Boolean = schoolId > 0

    val citizen: Person = Person(citizenId, InfectionStatus.withName(initialInfectionState), 0, isEmployee, isStudent)

    setCitizenInitialState(context, citizen)

    val home = House(homeId)
    val staysAt = Relation[Person, House](citizenId, "STAYS_AT", homeId)
    val memberOf = Relation[House, Person](homeId, "HOUSES", citizenId)
    val visits = Relation[Person, PublicPlace](citizenId, "VISITS", publicPlaceId)
    val hosts = Relation[PublicPlace, Person](publicPlaceId, "HOSTS", citizenId)

    val graphData = GraphData()
    graphData.addNode(citizenId, citizen)
    graphData.addNode(homeId, home)
    graphData.addNode(publicPlaceId, PublicPlace(publicPlaceId))
    graphData.addRelations(staysAt, memberOf, visits, hosts)

    if (isEmployee) {
      val office = Office(officeId)
      val worksAt = Relation[Person, Office](citizenId, "WORKS_AT", officeId)
      val employerOf = Relation[Office, Person](officeId, "EMPLOYER_OF", citizenId)
      graphData.addNode(officeId, office)
      graphData.addRelations(worksAt, employerOf)
    }

    if (isStudent) {
      val school = School(schoolId)
      val studiesAt = Relation[Person, School](citizenId, "STUDIES_AT", schoolId)
      val studentOf = Relation[School, Person](schoolId, "STUDENT_OF", citizenId)

      graphData.addNode(schoolId, school)
      graphData.addRelations(studiesAt, studentOf)
    }

    if (initialInfectionState == InfectedSevere.toString) {
      val hospitalId = 1
      val hospital = Hospital(1)
      val admittedAt = Relation[Person, Hospital](citizenId, "ADMITTED_AT", hospitalId)
      val admits = Relation[Hospital, Person](hospitalId, "ADMITS", citizenId)

      graphData.addNode(hospitalId, hospital)
      graphData.addRelations(admittedAt, admits)
    }

    graphData
  }

  def generatePublicPlaceId(): Int = {
    lastPublicPlaceId = (lastPublicPlaceId % TOTAL_PUBLIC_PLACES) + 1
    lastPublicPlaceId
  }

  private def setCitizenInitialState(context: Context, citizen: Person): Unit = {
    val initialState = citizen.infectionState.toString
    val isAsymptomatic: Boolean = biasedCoinToss(
      Disease.asymptomaticPopulationPercentage
    )
    val severeInfectionPercentage = Disease.severeInfectedPopulationPercentage
    val exposedDuration = Disease.exposedDurationProbabilityDistribution.sample()
    val preSymptomaticDuration =
      Disease.presymptomaticDurationProbabilityDistribution.sample()
    val mildSymptomaticDuration =
      Disease.mildSymptomaticDurationProbabilityDistribution.sample()
    val severeSymptomaticDuration =
      Disease.severeSymptomaticDurationProbabilityDistribution.sample()
    initialState match {
      case "Susceptible" => citizen.setInitialState(SusceptibleState())
      case "Exposed" =>
        citizen.setInitialState(ExposedState(severeInfectionPercentage, isAsymptomatic, exposedDuration))
      case "PreSymptomatic" => citizen.setInitialState(PreSymptomaticState(Mild, preSymptomaticDuration))
      case "InfectedMild"   => citizen.setInitialState(InfectedState(Mild, mildSymptomaticDuration))
      case "InfectedSevere" => citizen.setInitialState(InfectedState(Severe, severeSymptomaticDuration))
      case "Recovered"      => citizen.setInitialState(RecoveredState())
      case "Deceased"       => citizen.setInitialState(DeceasedState())
      case _                => throw new Exception(s"Unsupported infection status: $initialState")
    }
  }

  private def printStats(beforeCount: Int)(implicit context: Context): Unit = {
    val afterCountSusceptible = getSusceptibleCount(context)
    val afterExposedCount = getExposedCount(context)
    val afterCountInfected = getInfectedCount(context)
    val afterCountRecovered = context.graphProvider.fetchCount("Person", "infectionState" equ Recovered)
    val afterCountDeceased = context.graphProvider.fetchCount("Person", "infectionState" equ Deceased)

    logger.info("Infected before: {}", beforeCount)
    logger.info("Exposed: {}", afterExposedCount)
    logger.info("Infected after: {}", afterCountInfected)
    logger.info("Recovered: {}", afterCountRecovered)
    logger.info("Deceased: {}", afterCountDeceased)
    logger.info("Susceptible: {}", afterCountSusceptible)
  }

  private def getSusceptibleCount(context: Context) = {
    context.graphProvider.fetchCount("Person", "infectionState" equ Susceptible)
  }

  private def getExposedCount(context: Context) = {
    context.graphProvider.fetchCount("Person", "infectionState" equ Exposed)
  }

  private def getInfectedCount(context: Context): Int = {
    val condition =
      ("infectionState" equ Exposed) or ("infectionState" equ PreSymptomatic) or ("infectionState" equ InfectedMild) or ("infectionState" equ InfectedSevere) or ("infectionState" equ Asymptomatic)
    context.graphProvider.fetchCount("Person", condition)
  }
}

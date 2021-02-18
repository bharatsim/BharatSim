package com.bharatsim.model

import com.bharatsim.engine.ContextBuilder._
import com.bharatsim.engine._
import com.bharatsim.engine.actions.StopSimulation
import com.bharatsim.engine.basicConversions.decoders.DefaultDecoders._
import com.bharatsim.engine.basicConversions.encoders.DefaultEncoders._
import com.bharatsim.engine.dsl.SyntaxHelpers._
import com.bharatsim.engine.graph.ingestion.{GraphData, Relation}
import com.bharatsim.engine.graph.patternMatcher.MatchCondition._
import com.bharatsim.engine.intervention.IntervalBasedIntervention
import com.bharatsim.engine.listeners.{CsvOutputGenerator, SimulationListenerRegistry}
import com.bharatsim.engine.models.Agent
import com.bharatsim.engine.utils.Probability.biasedCoinToss
import com.bharatsim.model.InfectionSeverity.{Mild, Severe}
import com.bharatsim.model.InfectionStatus._
import com.bharatsim.model.diseaseState._
import com.typesafe.scalalogging.LazyLogging

object Main2 extends LazyLogging {
  private val TOTAL_PUBLIC_PLACES = 10
  private var lastPublicPlaceId = 1

  def main(args: Array[String]): Unit = {
    var beforeCount = 0
    val distributedSimulation = new DistributedSimulation

    distributedSimulation.ingestData{implicit context =>
      ingestCSVData("input.csv", csvDataExtractor)
    }

    distributedSimulation.defineSimulation{implicit context =>
      addLockdown

      createSchedules()

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
        new CsvOutputGenerator("src/main/resources/output.csv", new SEIROutputSpec(context))
      )
    }

    distributedSimulation.onCompleteSimulation{implicit context =>
      printStats(beforeCount)
      teardown()
    }

    distributedSimulation.run()
  }

  private def addLockdown(implicit context: Context): Unit = {

    val interventionName = "lockdown"
    val intervention = IntervalBasedIntervention(interventionName, 20, 530);

    val lockdownSchedule = (Day, Hour).add[House](0, 23);

    registerIntervention(intervention)
    registerSchedules(
      (
        lockdownSchedule,
        (agent: Agent, context: Context) => {
          val isEssentialWorker = agent.asInstanceOf[Person].isEssentialWorker
          val violateLockdown = agent.asInstanceOf[Person].violateLockdown
          val isLockdown = context.activeInterventionNames.contains(interventionName)
          val isSeverelyInfected = agent.asInstanceOf[Person].activeState == InfectedState(Severe)
          isLockdown && !(isEssentialWorker || violateLockdown || isSeverelyInfected)
        }
      )
    )
  }

  private def createSchedules()(implicit context: Context): Unit = {
    val employeeScheduleOnWeekDays = (Day, Hour)
      .add[House](0, 8)
      .add[Office](9, 18)
      .add[House](19, 23)

    val employeeScheduleOnWeekEnd = (Day, Hour)
      .add[House](0, 16)
      .add[PublicPlace](17, 18)
      .add[House](19, 23)

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
      .add[House](16, 16)
      .add[PublicPlace](17, 18)
      .add[House](19, 23)

    val studentScheduleOnWeekEnd = (Day, Hour)
      .add[House](0, 16)
      .add[PublicPlace](17, 18)
      .add[House](19, 23)

    val studentSchedule = (Week, Day)
      .add(studentScheduleOnWeekDay, 0, 4)
      .add(studentScheduleOnWeekEnd, 5, 6)

    val hospitalizedScheduleForDay = (Day, Hour)
      .add[Hospital](0, 23)

    val hospitalizedSchedule = (Week, Day)
      .add(hospitalizedScheduleForDay, 0, 6)

    registerSchedules(
      (
        hospitalizedSchedule,
        (agent: Agent, _: Context) => agent.asInstanceOf[Person].activeState == InfectedState(Severe)
      ),
      (
        employeeScheduleWithPublicTransport,
        (agent: Agent, _: Context) =>
          agent.asInstanceOf[Person].takesPublicTransport && agent.asInstanceOf[Person].age >= 30
      ),
      (employeeSchedule, (agent: Agent, _: Context) => agent.asInstanceOf[Person].age >= 30),
      (studentSchedule, (agent: Agent, _: Context) => agent.asInstanceOf[Person].age < 30)
    )
  }

  private def csvDataExtractor(map: Map[String, String])(implicit context: Context): GraphData = {

    val citizenId = map("id").toInt
    val age = map("age").toInt
    val takesPublicTransport = map("public_transport").toBoolean
    val isEssentialWorker = map("is_essential_worker").toBoolean
    val violateLockdown = map("violate_lockdown").toBoolean
    val initialInfectionState = map("infectionState")
    val villageTown = map("village_town")
    val lat = map("lattitude")
    val long = map("longitude")

    val citizen: Person = Person(
      citizenId,
      age,
      InfectionStatus.withName(initialInfectionState),
      0,
      takesPublicTransport,
      isEssentialWorker,
      violateLockdown,
      villageTown,
      lat,
      long
    )

    setCitizenInitialState(context, citizen, map("infectionState"))

    val homeId = map("house_id").toInt
    val schoolId = map("school_id").toInt
    val officeId = map("office_id").toInt
    val publicPlaceId = generatePublicPlaceId()

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

    val isEmployee = officeId > 0
    if (isEmployee) {
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

    if (takesPublicTransport) {
      val transportId = 1
      val transport = Transport(transportId)
      val takes = Relation[Person, Transport](citizenId, citizen.getRelation[Transport]().get, transportId)
      val carries = Relation[Transport, Person](transportId, transport.getRelation[Person]().get, citizenId)

      graphData.addNode(transportId, transport)
      graphData.addRelations(takes, carries)
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

  private def setCitizenInitialState(context: Context, citizen: Person, initialState: String): Unit = {
    val isAsymptomatic: Boolean = biasedCoinToss(Disease.asymptomaticPopulationPercentage)
    val severeInfectionPercentage = Disease.severeInfectedPopulationPercentage
    initialState match {
      case "Susceptible"    => citizen.setInitialState(SusceptibleState())
      case "Exposed"        => citizen.setInitialState(ExposedState(severeInfectionPercentage, isAsymptomatic))
      case "PreSymptomatic" => citizen.setInitialState(PreSymptomaticState(Mild))
      case "InfectedMild"   => citizen.setInitialState(InfectedState(Mild))
      case "InfectedSevere" => citizen.setInitialState(InfectedState(Severe))
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

package com.bharatsim.examples.epidemiology.tenCompartmentalModel

import java.util.Date
import java.util.concurrent.atomic.AtomicInteger

import com.bharatsim.engine.ContextBuilder._
import com.bharatsim.engine._
import com.bharatsim.engine.actions.StopSimulation
import com.bharatsim.engine.basicConversions.decoders.DefaultDecoders._
import com.bharatsim.engine.basicConversions.encoders.DefaultEncoders._
import com.bharatsim.engine.dsl.SyntaxHelpers._
import com.bharatsim.engine.execution.Simulation
import com.bharatsim.engine.graph.GraphNode
import com.bharatsim.engine.graph.ingestion.{GraphData, Relation}
import com.bharatsim.engine.graph.patternMatcher.EmptyPattern
import com.bharatsim.engine.graph.patternMatcher.MatchCondition._
import com.bharatsim.engine.intervention.{Intervention, SingleInvocationIntervention}
import com.bharatsim.engine.listeners.{CsvOutputGenerator, SimulationListenerRegistry}
import com.bharatsim.engine.models.Agent
import com.bharatsim.engine.utils.Probability.biasedCoinToss
import com.bharatsim.engine.utils.StreamUtil
import com.bharatsim.examples.epidemiology.tenCompartmentalModel.InfectionSeverity.{Mild, Severe}
import com.bharatsim.examples.epidemiology.tenCompartmentalModel.InfectionStatus._
import com.bharatsim.examples.epidemiology.tenCompartmentalModel.diseaseState._
import com.typesafe.scalalogging.LazyLogging

object Main extends LazyLogging {
  private val TOTAL_PUBLIC_PLACES = 10
  private var lastPublicPlaceId = 1
  //  TODO: Update initial infections - Jayanta / Philip
  private val initialInfectedFraction = 0.01
  private var ingestedPopulation = 0
  private val vaccinesAdministered = new AtomicInteger(0)
  private var vaccinationStarted = 0

  private val myTick: ScheduleUnit = new ScheduleUnit(1)
  private val myDay: ScheduleUnit = new ScheduleUnit(myTick * 2)

  private val hospitalIDs: List[Long] = List[Long](2001000000042L, 2001000000145L, 2001000000356L, 2001000000571L,
    2001000000600L, 2001000000609L, 2001000000663L, 2001000000779L, 2001000000894L, 2001000001040L, 2001000001199L,
    2001000001279L, 2001000001431L, 2001000001531L, 2001000001852L, 2001000001875L, 2001000002033L, 2001000002449L,
    2001000002615L, 2001000002758L, 2001000002921L, 2001000003211L, 2001000003416L, 2001000003694L, 2001000004265L,
    2001000004754L, 2001000005122L, 2001000005159L, 2001000005304L, 2001000005411L, 2001000005520L, 2001000005596L,
    2001000005630L, 2001000005653L, 2001000005685L, 2001000005844L, 2001000006153L, 2001000006327L, 2001000006509L,
    2001000006542L, 2001000006713L, 2001000006786L, 2001000007064L, 2001000007067L, 2001000007075L, 2001000007093L,
    2001000007296L, 2001000007373L, 2001000007393L, 2001000007540L, 2001000007590L, 2001000007601L, 2001000007804L,
    2001000007844L, 2001000008179L, 2001000008698L, 2001000008797L, 2001000009197L, 2001000009360L, 2001000009675L,
    2001000009745L, 2001000010236L, 2001000010844L, 2001000010898L, 2001000011416L, 2001000011628L, 2001000011998L,
    2001000012007L)

//  private val hospitalIDs = List.range(1,280)

  private val TOTAL_HOSPITALS = hospitalIDs.length

  private val random = new scala.util.Random()

  def main(args: Array[String]): Unit = {
    var beforeCount = 0
    val simulation = Simulation()
    simulation.ingestData { implicit context =>
      ingestCSVData("input.csv", csvDataExtractor)

      logger.debug("Ingestion done")
    }

    simulation.defineSimulation { implicit context =>
      ingestedPopulation = context.graphProvider.fetchCount("Person", EmptyPattern())
      addLockdown
      vaccination

      create12HourSchedules()

      registerAction(
        StopSimulation,
        (c: Context) => {
          getInfectedCount(c) == 0
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
      registerState[HospitalizedState]

      val currentTime = new Date().getTime

      SimulationListenerRegistry.register(
        new CsvOutputGenerator("src/main/resources/output_" + currentTime + ".csv", new SEIROutputSpec(context))
      )
      SimulationListenerRegistry.register(
        new CsvOutputGenerator("src/main/resources/GIS_output_" + currentTime + ".csv", new GISOutputSpec(context))
      )

    }

    simulation.onCompleteSimulation { implicit context =>
      printStats(beforeCount)
      teardown()

    }

    val startTime = System.currentTimeMillis()
    simulation.run()
    val endTime = System.currentTimeMillis()
    logger.info("Total time: {} s", (endTime - startTime) / 1000)
  }

  private def vaccination(implicit context: Context): Unit = {
    var interventionActivatedAt = 0
    val interventionName = "vaccination"
    val activationCondition = (context: Context) => {
      val result = context.activeInterventionNames.contains("lockdown")
      if (result) {
        vaccinationStarted = context.getCurrentStep
      }
      result
    }
    val firstTimeExecution = (context: Context) => interventionActivatedAt = context.getCurrentStep
    val deActivationCondition = (context: Context) => {
      vaccinesAdministered.get() >= ingestedPopulation
    }

    val intervention: Intervention = SingleInvocationIntervention(
      interventionName,
      activationCondition,
      deActivationCondition,
      firstTimeExecution,
      context => {
        //      TODO: Add shuffle while picking up agents - Jayanta / Philip
        //      TODO: Update vaccination strategy - Jayanta / Philip
        val populationIterator: Iterator[GraphNode] = context.graphProvider.fetchNodes("Person")
        val numberOfVaccinesPerTick = Disease.vaccinationRate * ingestedPopulation * Disease.dt

        StreamUtil
          .create(populationIterator, parallel = true)
          .filter((node) => node.as[Person].shouldGetVaccine())
          .limit(numberOfVaccinesPerTick.toLong)
          .forEach(node => {
            val person = node.as[Person]

            person.updateParams(
              ("vaccinationStatus", true),
              ("betaMultiplier", person.betaMultiplier * Disease.vaccinatedBetaMultiplier),
              ("gamma", person.gamma * (1 + Disease.vaccinatedGammaFractionalIncrease))
            )
            vaccinesAdministered.getAndIncrement()
          })
      }
    )

    registerIntervention(intervention)
  }

  private def addLockdown(implicit context: Context): Unit = {

    var interventionActivatedAt = 0

    val interventionName = "lockdown"
    val activationCondition = (context: Context) => getInfectedCount(context) >= ingestedPopulation.toDouble * 0.05
    val firstTimeExecution = (context: Context) => interventionActivatedAt = context.getCurrentStep
    val deActivationCondition = (context: Context) => {
      context.getCurrentStep >= interventionActivatedAt + (30 * Disease.inverse_dt).toInt
    }
    val intervention =
      SingleInvocationIntervention(interventionName, activationCondition, deActivationCondition, firstTimeExecution)

    val lockdownSchedule = (myDay, myTick).add[House](0, 1)

    registerIntervention(intervention)
    registerSchedules(
      (
        lockdownSchedule,
        (agent: Agent, context: Context) => {
          val isEssentialWorker = agent.asInstanceOf[Person].isEssentialWorker || agent.asInstanceOf[Person].isHCW
          val violateLockdown = agent.asInstanceOf[Person].violateLockdown
          val isLockdown = context.activeInterventionNames.contains(interventionName)
          isLockdown && !(isEssentialWorker || violateLockdown)
        },
        1
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

    val homeBoundScheduleForDay = (Day, Hour)
      .add[House](0, 23)

    val homeBoundScheduleForWeek = (Week, Day)
      .add(homeBoundScheduleForDay, 0, 6)

    registerSchedules(
      (
        hospitalizedSchedule,
        (agent: Agent, _: Context) => agent.asInstanceOf[Person].isSevereInfected,
        2
      ),
      (
        homeBoundScheduleForWeek,
        (agent: Agent, _: Context) =>
          agent.asInstanceOf[Person].isMildInfected || agent.asInstanceOf[Person].isHomeBound,
        3
      ),
      (
        employeeScheduleWithPublicTransport,
        (agent: Agent, _: Context) => agent.asInstanceOf[Person].takesPublicTransport,
        4
      ),
      (employeeSchedule, (agent: Agent, _: Context) => agent.asInstanceOf[Person].isEmployee, 5),
      (studentSchedule, (agent: Agent, _: Context) => agent.asInstanceOf[Person].isStudent, 6)
    )
  }

  private def create12HourSchedules()(implicit context: Context): Unit = {
    val employeeSchedule = (myDay, myTick)
      .add[House](0, 0)
      .add[Office](1, 1)

    val studentSchedule = (myDay, myTick)
      .add[House](0, 0)
      .add[School](1, 1)

    val homeBoundSchedule = (myDay, myTick)
      .add[House](0, 1)

    val hospitalizedSchedule = (myDay, myTick)
      .add[Hospital](0, 1)

    val hcwSchedule = (myDay, myTick)
      .add[House](0, 0)
      .add[Hospital](1, 1)

    registerSchedules(
      (hospitalizedSchedule, (agent: Agent, _: Context) => agent.asInstanceOf[Person].isHospitalized, 2),
      (
        homeBoundSchedule,
        (agent: Agent, _: Context) =>
          agent.asInstanceOf[Person].isSevereInfected || agent.asInstanceOf[Person].isHomeBound,
        3
      ),
      (hcwSchedule, (agent: Agent, _: Context) => agent.asInstanceOf[Person].isHCW, 4),
      (employeeSchedule, (agent: Agent, _: Context) => agent.asInstanceOf[Person].isEmployee, 5),
      (studentSchedule, (agent: Agent, _: Context) => agent.asInstanceOf[Person].isStudent, 6)
    )
  }

  private def roundToAgeRange(age: Int): Int = {
    (age / 10) * 10 + 9
  }

  private def csvDataExtractor(map: Map[String, String])(implicit context: Context): GraphData = {

    val citizenId = map("Agent_ID").toLong
    val age = map("Age").toInt
    val takesPublicTransport = map("PublicTransport_Jobs").toInt == 1
    val isEssentialWorker = map("essential_worker").toInt == 1
    val violateLockdown = map("Adherence_to_Intervention").toFloat < 0.5
    val initialInfectionState = if (biasedCoinToss(initialInfectedFraction)) "Asymptomatic" else "Susceptible"
    val villageTown = map("AdminUnitName")
    val lat = map("H_Lat")
    val long = map("H_Lon")

    val homeId = map("HHID").toLong
    val schoolId = map("school_id").toLong
    val officeId = map("WorkPlaceID").toLong
    val publicPlaceId = generatePublicPlaceId()

    val isHCW: Boolean = hospitalIDs.contains(officeId)
    val isEmployee: Boolean = officeId > 0
    val isStudent: Boolean = schoolId > 0

    val betaMultiplier: Double =
      Disease.ageStratifiedBetaMultiplier.getOrElse(roundToAgeRange(age), Disease.ageStratifiedBetaMultiplier(99))
    val gamma: Double =
      1.0 - Disease.ageStratifiedOneMinusGamma.getOrElse(roundToAgeRange(age), Disease.ageStratifiedOneMinusGamma(99))
    val delta: Double =
      1.0 - Disease.ageStratifiedOneMinusDelta.getOrElse(roundToAgeRange(age), Disease.ageStratifiedOneMinusDelta(99))
    val sigma: Double = Disease.ageStratifiedSigma.getOrElse(roundToAgeRange(age), Disease.ageStratifiedSigma(99))

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
      long,
      isEmployee,
      isStudent,
      betaMultiplier,
      gamma,
      delta,
      sigma,
      isHCW
    )

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

    val hospitalId = if (isHCW) officeId else hospitalIDs(random.nextInt(TOTAL_HOSPITALS))

    val hospital = Hospital(hospitalId)
    val visitsHospital = Relation[Person, Hospital](citizenId, "TREATED_AT", hospitalId)
    val hospitalVisited = Relation[Hospital, Person](hospitalId, "TREATS", citizenId)

    graphData.addNode(hospitalId, hospital)
    graphData.addRelations(visitsHospital, hospitalVisited)

    if (isEmployee && !isHCW) {
      val office = Office(officeId)
      val worksAt = Relation[Person, Office](citizenId, "WORKS_AT", officeId)
      val employerOf = Relation[Office, Person](officeId, "EMPLOYER_OF", citizenId)

      graphData.addNode(officeId, office)
      graphData.addRelations(worksAt, employerOf)
    } else if (isStudent && !isHCW) {
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

//    ingestedPopulation = ingestedPopulation + 1
    graphData
  }

  def generatePublicPlaceId(): Int = {
    lastPublicPlaceId = (lastPublicPlaceId % TOTAL_PUBLIC_PLACES) + 1
    lastPublicPlaceId
  }

  private def setCitizenInitialState(context: Context, citizen: Person): Unit = {
    val initialState = citizen.infectionState.toString
    val exposedDuration = Disease.exposedDurationProbabilityDistribution.sample()
    //    TODO: Add the change to master - Jayanta / Philip
    val asymptomaticDuration = Disease.asymptomaticDurationProbabilityDistribution.sample()
    val preSymptomaticDuration = Disease.presymptomaticDurationProbabilityDistribution.sample()
    val mildSymptomaticDuration = Disease.mildSymptomaticDurationProbabilityDistribution.sample()
    val severeSymptomaticDuration = Disease.severeSymptomaticDurationProbabilityDistribution.sample()
    val hospitalizedDuration = Disease.criticalSymptomaticDurationProbabilityDistribution.sample()
    initialState match {
      case "Susceptible"    => citizen.setInitialState(SusceptibleState())
      case "Exposed"        => citizen.setInitialState(ExposedState(exposedDuration))
      case "Asymptomatic"   => citizen.setInitialState(AsymptomaticState(asymptomaticDuration))
      case "PreSymptomatic" => citizen.setInitialState(PreSymptomaticState(Mild, preSymptomaticDuration))
      case "InfectedMild"   => citizen.setInitialState(InfectedState(Mild, mildSymptomaticDuration))
      case "InfectedSevere" => citizen.setInitialState(InfectedState(Severe, severeSymptomaticDuration))
      case "Hospitalized"   => citizen.setInitialState(HospitalizedState(hospitalizedDuration))
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

    logger.info("Vaccination started at: {}", vaccinationStarted)
    logger.info("Total vaccines administered: {}", vaccinesAdministered)
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
      ("infectionState" equ Susceptible) or ("infectionState" equ Recovered) or ("infectionState" equ Deceased)
    ingestedPopulation - context.graphProvider.fetchCount("Person", condition)
  }
}

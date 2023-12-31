package com.bharatsim.examples.epidemiology.tenCompartmentalModel

import com.bharatsim.engine.utils.Probability.biasedCoinToss
import com.github.tototoshi.csv.CSVWriter

import scala.annotation.tailrec
import scala.util.Random

object DummyDataGenerator {
  val headers = List(
    "Agent_ID",
    "Age",
    "PublicTransport_Jobs",
    "essential_worker",
    "Adherence_to_Intervention",
    "AdminUnitName",
    "H_Lat",
    "H_Lon",
    "HHID",
    "school_id",
    "WorkPlaceID"
  )

  val totalPopulation = 10_000
  val ESSENTIAL_WORKER_FRACTION = 0.2
  val PUBLIC_TRANSPORT_FRACTION = 0.3

  private val averageEmployeesPerOffice = 100
  val totalOffices = (totalPopulation / 2) / averageEmployeesPerOffice

  val averageStudentsPerSchool = 100
  val totalSchools = (totalPopulation / 2) / averageStudentsPerSchool

  val random = new Random()

  val writer = CSVWriter.open("dummy10stateModel10k.csv")

  @tailrec
  private def generateRow(rowNum: Int): Unit = {
    val id = rowNum
    val age = random.between(5, 80)
    val houseId = random.between(1, totalPopulation / 4 + 1)
    val isHomeBound = random.nextDouble() <= 0.3
    val isEmployee = if (isHomeBound) false else age >= 30
    val isStudent = if (isHomeBound) false else !isEmployee
    val officeId = if (isEmployee) random.between(1, totalOffices + 1) else 0
    val schoolId = if (isStudent) random.between(1, totalSchools + 1) else 0
    val publicTransport = if (biasedCoinToss(PUBLIC_TRANSPORT_FRACTION)) 1 else 0
    val isEssentialWorker = if (isEmployee && biasedCoinToss(ESSENTIAL_WORKER_FRACTION)) 1 else 0
    //    TODO: Add the change to master - Jayanta / Philip
    val violatesLockdown: Double = if (biasedCoinToss(0.1)) random.between(0.0, 0.5) else random.between(0.5, 1.0)
    val scale = math pow (10, 1)
    val village_town = "some_village"
    val latitude = Random.nextFloat()
    val longitude = Random.nextFloat()

    writer.writeRow(
      List(
        id,
        age,
        publicTransport,
        isEssentialWorker,
        (math round violatesLockdown * scale) / scale,
        village_town,
        latitude,
        longitude,
        houseId,
        schoolId,
        officeId
      )
    )

    if (rowNum < totalPopulation) {
      generateRow(rowNum + 1)
    }
  }

  private def generate(): Unit = {
    println("Total schools", totalSchools)
    println("Total offices", totalOffices)
    writer.writeRow(headers)
    generateRow(1)
  }

  def main(): Unit = {
    generate()
  }
}

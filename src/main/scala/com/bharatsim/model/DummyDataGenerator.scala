
package com.bharatsim.model

import com.bharatsim.engine.utils.Probability.biasedCoinToss
import com.github.tototoshi.csv.CSVWriter

import scala.annotation.tailrec
import scala.util.Random

object DummyDataGenerator {
  val headers = List(
    "id",
    "label",
    "age",
    "infectionState",
    "house_id",
    "office_id",
    "school_id",
    "public_transport",
    "is_essential_worker",
    "violate_lockdown",
    "village_town",
    "lattitude",
    "longitude"
  )

  val totalPopulation = 10_000
  val INITIAL_INFECTED_PERCENTAGE = 0.04
  val ESSENTIAL_WORKER_PERCENTAGE = 0.2
  val LEAKAGE_PERCENTAGE = 0.1

  private val averageEmployeesPerOffice = 100
  val totalOffices = (totalPopulation / 2) / averageEmployeesPerOffice

  val averageStudentsPerSchool = 100
  val totalSchools = (totalPopulation / 2) / averageStudentsPerSchool

  val random = new Random()

  val writer = CSVWriter.open("dummydata.csv")

  @tailrec
  private def generateRow(rowNum: Int): Unit = {
    val id = rowNum
    val label = "Citizen"
    val age = random.between(10, 51)
    val infectionStatus = if (biasedCoinToss(INITIAL_INFECTED_PERCENTAGE)) "InfectedMild" else "Susceptible"
    val houseId = random.between(1, totalPopulation / 4 + 1)
    val isEmployee = age >= 30
    val isStudent = !isEmployee
    val officeId = if (isEmployee) random.between(1, totalOffices + 1) else 0
    val schoolId = if (isStudent) random.between(1, totalSchools + 1) else 0
    val publicTransport = random.nextBoolean()
    val isEssentialWorker = if (isEmployee) biasedCoinToss(ESSENTIAL_WORKER_PERCENTAGE) else false
    val violatesLockdown = biasedCoinToss(LEAKAGE_PERCENTAGE)
    val village_town = "some_village"
    val lattitude = Random.nextFloat()
    val longitude = Random.nextFloat()

    writer.writeRow(
      List(
        id,
        label,
        age,
        infectionStatus,
        houseId,
        officeId,
        schoolId,
        publicTransport,
        isEssentialWorker,
        violatesLockdown,
        village_town,
        lattitude,
        longitude
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

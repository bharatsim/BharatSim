package com.bharatsim.examples.epidemiology.tenCompartmentalModel.diseaseState

import com.bharatsim.engine.Context
import com.bharatsim.engine.basicConversions.decoders.DefaultDecoders._
import com.bharatsim.engine.basicConversions.encoders.DefaultEncoders._
import com.bharatsim.engine.fsm.State
import com.bharatsim.engine.graph.patternMatcher.MatchCondition._
import com.bharatsim.engine.models.{Network, StatefulAgent}
import com.bharatsim.engine.utils.Probability.biasedCoinToss
import com.bharatsim.examples.epidemiology.tenCompartmentalModel.InfectionStatus._
import com.bharatsim.examples.epidemiology.tenCompartmentalModel.{Disease, Person}

case class SusceptibleState() extends State {
  //    TODO: Add the change to master - Jayanta / Philip
  def shouldBeInfected(context: Context, agent: StatefulAgent): Boolean = {
    if (agent.activeState == SusceptibleState()) {
      val infectionRate = context.dynamics.asInstanceOf[Disease.type].beta
      val dt = context.dynamics.asInstanceOf[Disease.type].dt

      val schedule = context.fetchScheduleFor(agent).get

      val currentStep = context.getCurrentStep
      val placeType: String = schedule.getForStep(currentStep)

      val places = agent.getConnections(agent.getRelation(placeType).get).toList
      if (places.nonEmpty) {
        val place = places.head
        val decodedPlace = agent.asInstanceOf[Person].decodeNode(placeType, place)

        val infectedFraction = fetchInfectedFraction(decodedPlace, placeType, context)
        return biasedCoinToss(agent.asInstanceOf[Person].betaMultiplier * infectionRate * infectedFraction * dt)
      }
    }
    false
  }

  private def fetchInfectedFraction(decodedPlace: Network, place: String, context: Context): Double = {
    val cache = context.perTickCache

    val tuple = (place, decodedPlace.internalId)
    cache.getOrUpdate(tuple, () => fetchFromStore(decodedPlace)).asInstanceOf[Double]
  }

  private def fetchFromStore(decodedPlace: Network): Double = {
    val infectedPattern =
      ("infectionState" equ InfectedMild) or ("infectionState" equ InfectedSevere) or ("infectionState" equ Asymptomatic) or ("infectionState" equ PreSymptomatic) or ("infectionState" equ Hospitalized)
    val total = decodedPlace.getConnectionCount(decodedPlace.getRelation[Person]().get)
    val infectedUnvaccinated = decodedPlace
      .getConnectionCount(decodedPlace.getRelation[Person]().get, ("vaccinationStatus" equ false) and infectedPattern)
    val infectedVaccinated = decodedPlace
      .getConnectionCount(decodedPlace.getRelation[Person]().get, ("vaccinationStatus" equ true) and infectedPattern)

    if (total == 0.0)
      return 0.0

    (infectedUnvaccinated.toDouble +
      (infectedVaccinated.toDouble * (1 - Disease.fractionalTransmissionReduction))) / total.toDouble
  }

  addTransition(
    when = shouldBeInfected,
    to = context =>
      ExposedState(
        Disease.exposedDurationProbabilityDistribution.sample()
      )
  )
}

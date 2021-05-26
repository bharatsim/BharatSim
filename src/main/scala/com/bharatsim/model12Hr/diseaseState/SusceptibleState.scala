package com.bharatsim.model12Hr.diseaseState

import com.bharatsim.engine.Context
import com.bharatsim.engine.basicConversions.decoders.DefaultDecoders._
import com.bharatsim.engine.basicConversions.encoders.DefaultEncoders._
import com.bharatsim.engine.fsm.State
import com.bharatsim.engine.graph.patternMatcher.MatchCondition._
import com.bharatsim.engine.models.{Network, StatefulAgent}
import com.bharatsim.engine.utils.Probability.{biasedCoinToss, toss}
import com.bharatsim.model12Hr.InfectionStatus._
import com.bharatsim.model12Hr.{Disease, Person}

case class SusceptibleState() extends State {

  def shouldInfect(context: Context, agent: StatefulAgent): Boolean = {
    if (agent.activeState == SusceptibleState()) {
      val infectionRate = Disease.infectionRate
      val dt = Disease.dt

      val schedule = context.fetchScheduleFor(agent).get

      val currentStep = context.getCurrentStep
      val placeType: String = schedule.getForStep(currentStep)

      val places = agent.getConnections(agent.getRelation(placeType).get).toList
      if (places.nonEmpty) {
        val place = places.head
        val decodedPlace = agent.asInstanceOf[Person].decodeNode(placeType, place)

        val infectedFraction = fetchInfectedFraction(decodedPlace, placeType, context)

        return biasedCoinToss(infectionRate * dt * infectedFraction)
      }
    }
    false
  }

  private def fetchInfectedFraction(decodedPlace: Network, place: String, context: Context): Double = {
    val cache = context.perTickCache

    cache.getOrUpdate((place, decodedPlace.internalId), () => fetchFromStore(decodedPlace)).asInstanceOf[Double]
  }

  private def fetchFromStore(decodedPlace: Network): Double = {
    val total = decodedPlace
      .getConnectionCount(
        decodedPlace.getRelation[Person]().get
      )
    val infected = decodedPlace
      .getConnectionCount(
        decodedPlace.getRelation[Person]().get,
        ("infectionState" equ InfectedMild) or ("infectionState" equ InfectedSevere) or ("infectionState" equ Asymptomatic) or ("infectionState" equ PreSymptomatic)
      )
    if (total == 0.0)
      return 0.0

    infected.toDouble / total.toDouble
  }

  addTransition(
    when = shouldInfect,
    to = context =>
      ExposedState(
        Disease.severeInfectedPopulationPercentage,
        biasedCoinToss(Disease.asymptomaticPopulationPercentage),
        Disease.exposedDurationProbabilityDistribution.sample()
      )
  )
}

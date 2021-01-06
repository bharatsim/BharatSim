package com.bharatsim.model.diseaseState

import com.bharatsim.engine.Context
import com.bharatsim.engine.basicConversions.decoders.DefaultDecoders._
import com.bharatsim.engine.basicConversions.encoders.DefaultEncoders._
import com.bharatsim.engine.fsm.State
import com.bharatsim.engine.graph.patternMatcher.MatchCondition._
import com.bharatsim.engine.models.{Network, StatefulAgent}
import com.bharatsim.engine.utils.Probability.{biasedCoinToss, toss}
import com.bharatsim.model.InfectionStatus._
import com.bharatsim.model.{Disease, NeighborCache, Person}

case class SusceptibleState() extends State {

  def shouldInfect(context: Context, agent: StatefulAgent): Boolean = {
    if (agent.activeState == SusceptibleState()) {
      val infectionRate = context.dynamics.asInstanceOf[Disease.type].infectionRate

      val schedule = context.fetchScheduleFor(agent).get

      val currentStep = context.getCurrentStep
      val placeType: String = schedule.getForStep(currentStep)

      val places = agent.getConnections(agent.getRelation(placeType).get).toList
      if (places.nonEmpty) {
        val place = places.head
        val decodedPlace = agent.asInstanceOf[Person].decodeNode(placeType, place)

        val infectedNeighbourCount = infectedNeighborsCount(decodedPlace, placeType, context.getCurrentStep)

        return biasedCoinToss(decodedPlace.getContactProbability()) && toss(infectionRate, infectedNeighbourCount)
      }
    }
    false
  }

  private def infectedNeighborsCount(decodedPlace: Network, place: String, step: Int) = {
    NeighborCache.countFor(place, decodedPlace.internalId, step) match {
      case Some(count) => count
      case _ =>
        val count = decodedPlace
          .getConnectionCount(
            decodedPlace.getRelation[Person]().get,
            ("infectionState" equ InfectedMild) or ("infectionState" equ InfectedSevere)
          )
        NeighborCache.put(place, decodedPlace.internalId, count, step)
        count
    }
  }

  addTransition(
    when = shouldInfect,
    to = context =>
      ExposedState(
        context.dynamics.asInstanceOf[Disease.type].severeInfectedPopulationPercentage,
        biasedCoinToss(context.dynamics.asInstanceOf[Disease.type].asymptomaticPopulationPercentage)
      )
  )
}

package com.bharatsim.model.diseaseState

import com.bharatsim.engine.Context
import com.bharatsim.engine.basicConversions.decoders.DefaultDecoders._
import com.bharatsim.engine.basicConversions.encoders.DefaultEncoders._
import com.bharatsim.engine.fsm.State
import com.bharatsim.engine.graph.GraphNode
import com.bharatsim.engine.graph.patternMatcher.MatchCondition._
import com.bharatsim.engine.models.{Network, StatefulAgent}
import com.bharatsim.engine.utils.Probability.{biasedCoinToss, toss}
import com.bharatsim.model.{Disease, House, Office, Person, School, Transport}
import com.bharatsim.model.InfectionStatus._

case class SusceptibleState() extends State{

  def shouldInfect(context: Context, agent: StatefulAgent): Boolean = {
    if (agent.asInstanceOf[Person].isSusceptible) {
      val infectionRate = context.dynamics.asInstanceOf[Disease.type].infectionRate

      val schedule = context.fetchScheduleFor(agent).get

      val currentStep = context.getCurrentStep
      val placeType: String = schedule.getForStep(currentStep)

      val places = agent.getConnections(agent.getRelation(placeType).get).toList
      if (places.nonEmpty) {
        val place = places.head
        val decodedPlace = agent.asInstanceOf[Person].decodeNode(placeType, place)

        val infectedNeighbourCount = decodedPlace
          .getConnectionCount(decodedPlace.getRelation[Person]().get, "infectionState" equ Infected)

        val shouldInfect = biasedCoinToss(decodedPlace.getContactProbability()) && toss(infectionRate, infectedNeighbourCount)
        if (shouldInfect) {
          agent.updateParam("infectionState", Exposed)
        }
        return shouldInfect
      }
    }
    false
  }

  addTransition(
    when = shouldInfect,
    to = context => ExposedState()
  )
}

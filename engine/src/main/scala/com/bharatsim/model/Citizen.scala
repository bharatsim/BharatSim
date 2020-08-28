package com.bharatsim.model

import com.bharatsim.engine.{Agent, Context}

import scala.util.Random

class Citizen extends Agent {
  var infectionStatus: InfectionStatus = Susceptible;

  def isInfected(): Boolean = {
    infectionStatus == Infected
  }
  addBehaviour((context: Context) => {
    if (infectionStatus == Susceptible) {
      val infectionRate =
        context.dynamics.asInstanceOf[Disease.type].infectionRate;
      val infectedCount =
        context.agents.getAll().count(_.asInstanceOf[Citizen].isInfected())
      val shouldInfect =
        Random.between(1, 100) <= infectionRate * infectedCount;
      if (shouldInfect) infectionStatus = Infected
    }
  });

}

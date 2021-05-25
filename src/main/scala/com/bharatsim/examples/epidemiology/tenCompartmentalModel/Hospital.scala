package com.bharatsim.examples.epidemiology.tenCompartmentalModel

import com.bharatsim.engine.models.Network

case class Hospital(id: Long) extends Network {
  addRelation[Person]("TREATS")

  override def getContactProbability(): Double = 1.0
}

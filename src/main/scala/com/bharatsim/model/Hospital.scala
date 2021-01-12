package com.bharatsim.model

import com.bharatsim.engine.models.Network

case class Hospital(id: Int) extends Network {
  addRelation[Person]("ADMITS")

  override def getContactProbability(): Double = 0.05
}

package com.bharatsim.model

import com.bharatsim.engine.models.Network

case class Transport(id: Int) extends Network {
  addRelation[Person]("CARRIES")

  override def getContactProbability(): Double = 0.1
}

package com.bharatsim.model

import com.bharatsim.engine.models.Network

case class House(id: Int) extends Network {
  addRelation[Person]("HOUSES")

  override def getContactProbability(): Double = 1
}

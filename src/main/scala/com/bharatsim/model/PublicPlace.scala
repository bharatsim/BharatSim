package com.bharatsim.model

import com.bharatsim.engine.models.Network

case class PublicPlace(id: Int) extends Network {
  addRelation[Person]("HOSTS")

  override def getContactProbability(): Double = 0.05
}

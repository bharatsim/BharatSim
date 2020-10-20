package com.bharatsim.model

import com.bharatsim.engine.models.Node

case class House(id: Int) extends Node{
  addRelation[Citizen]("HOUSES")
}

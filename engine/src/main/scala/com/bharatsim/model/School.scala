package com.bharatsim.model

import com.bharatsim.engine.Node

case class School(id: Int) extends Node{
  addRelation[Citizen]("TEACHES")
}

package com.bharatsim.model

import com.bharatsim.engine.models.Node

case class School(id: Int) extends Node{
  addRelation[Person]("TEACHES")
}

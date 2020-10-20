package com.bharatsim.model

import com.bharatsim.engine.models.Node

case class Office(id: Int) extends Node{
  addRelation[Person]("EMPLOYER_OF")
}

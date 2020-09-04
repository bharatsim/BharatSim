package com.bharatsim.model

import com.bharatsim.engine.Node

class House extends Node {
  val houseNo: Option[String] = None;

  def addMember(citizen: Citizen): Unit = {
    unidirectionalConnect("home_for", citizen);
    citizen.setHome(this);
  }

  def getMember(): Iterator[Citizen] = {
    getConnections("home_for").map(_.asInstanceOf[Citizen]);
  }
}

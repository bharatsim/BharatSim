package com.bharatsim.engine.testModels

import com.bharatsim.engine.fsm.State
import com.bharatsim.engine.models.StatefulAgent

case class StatefulPerson(name: String, age: Int) extends StatefulAgent {

}

object TestFSM {
  case class IdleState(idleFor: Int) extends State
}

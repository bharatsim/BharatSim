package com.bharatsim.engine.testModels

import com.bharatsim.engine.Context
import com.bharatsim.engine.basicConversions.decoders.DefaultDecoders._
import com.bharatsim.engine.basicConversions.encoders.DefaultEncoders._
import com.bharatsim.engine.fsm.State
import com.bharatsim.engine.models.StatefulAgent

case class StatefulPerson(name: String, age: Int) extends StatefulAgent {}

object TestFSM {

  case class IdleState(idleFor: Int) extends State {
    override def enterAction(context: Context, agent: StatefulAgent): Unit = {
      agent.asInstanceOf[StatefulPerson].updateParam("name", "Santosh")
    }

    override def perTickAction(context: Context, agent: StatefulAgent): Unit = {
      updateParam("idleFor", idleFor + 1)
    }
  }

  case class NoTransitionState(perTickActionInvokedTimes: Int) extends State {

    addTransition((_, _) => false, IdleState(0))

    override def perTickAction(context: Context, agent: StatefulAgent): Unit = {
      updateParam("perTickActionInvokedTimes", perTickActionInvokedTimes + 1)
    }
  }

  case class StateWithTransition() extends State {
    var perTickActionInvokedTimes = 0
    addTransition((_, _) => true, IdleState(0))

    override def perTickAction(context: Context, agent: StatefulAgent): Unit = {
      perTickActionInvokedTimes += 1
    }
  }

}

package com.bharatsim.engine.testModels

import com.bharatsim.engine.Context
import com.bharatsim.engine.graph.GraphProvider.NodeId
import com.bharatsim.engine.models.Agent
import com.bharatsim.engine.testModels.Student.{studentBehaviour1, studentBehaviour2}
import org.mockito.MockitoSugar.spyLambda

case class Student() extends Agent {
  val goToSchool: Context => Unit = studentBehaviour1
  val playAGame: Context => Unit = studentBehaviour2

  addBehaviour(goToSchool)
  addBehaviour(playAGame)
}

object Student {
  val goToSchool: NodeId => Unit = spyLambda[NodeId => Unit]((_: Int) => {})
  val playAGame: NodeId => Unit = spyLambda[NodeId => Unit]((_: Int) => {})

  val studentBehaviour1: Context => Unit = spyLambda[Context => Unit]((context: Context) => {
    goToSchool(context.getCurrentStep)
  })

  val studentBehaviour2: Context => Unit = spyLambda[Context => Unit]((context: Context) => {
    playAGame(context.getCurrentStep)
  })
}

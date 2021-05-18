package com.bharatsim.engine.execution

import com.bharatsim.engine.Context
import com.bharatsim.engine.execution.actions.{PostTickActions, PreTickActions}
import com.typesafe.scalalogging.LazyLogging

import scala.collection.parallel.CollectionConverters.IterableIsParallelizable
import scala.collection.parallel.{ExecutionContextTaskSupport, TaskSupport}

class Tick(
    tick: Int,
    context: Context,
    preTickActions: PreTickActions,
    agentExecutor: AgentExecutor,
    postTickActions: PostTickActions
) extends LazyLogging {
  def preStepActions(): Unit = {
    preTickActions.execute(tick)
  }

  def exec(): Unit = {
    context.registeredNodesWithDecoder
      .foreach(nodeWithDecoder => agentExecutor.execute(nodeWithDecoder))
  }

  def execParallel(): Unit = {
    val nodesWithDecoder = context.registeredNodesWithDecoder
    val parallelizedCollection = nodesWithDecoder.par
    parallelizedCollection.tasksupport = Tick.getTaskSupport

    parallelizedCollection
      .foreach(nodeWithDecoder => agentExecutor.execute(nodeWithDecoder))
  }

  def postStepActions(): Unit = {
    postTickActions.execute()
  }
}

object Tick {
  val getTaskSupport: TaskSupport = new ExecutionContextTaskSupport()
}

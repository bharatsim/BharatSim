package com.bharatsim.engine.distributed.worker

import akka.Done
import akka.actor.typed.ActorSystem
import akka.stream.scaladsl.Source
import com.bharatsim.engine.execution.control.DistributeStateControl
import com.bharatsim.engine.graph.GraphNode
import com.bharatsim.engine.models.{Agent, StatefulAgent}
import com.bharatsim.engine.{ApplicationConfigFactory, Context}

import scala.concurrent.{ExecutionContextExecutor, Future}

class DistributedAgentProcessor() {
  val config = ApplicationConfigFactory.config
  type AgentsWithState = Iterable[(GraphNode, Option[GraphNode])]

  def process(nodes: AgentsWithState, simulationContext: Context, agentExecutor: DistributedAgentExecutor)(implicit
      system: ActorSystem[_]
  ): Future[Done] = {
    val behaviourControl = agentExecutor.behaviourControl
    val stateControl = agentExecutor.stateControl
    implicit val executionContext: ExecutionContextExecutor = system.executionContext
    Source(nodes.toList)
      .mapAsyncUnordered(config.agentProcessParallelism)(nodeWithState =>
        Future {
          decodeAndAssignState(nodeWithState._1, nodeWithState._2, simulationContext)
        }
      )
      .mapAsyncUnordered(config.agentProcessParallelism)({ agent =>
        Future {
          behaviourControl.executeFor(agent)
          agent
        }
      })
      .mapAsyncUnordered(config.agentProcessParallelism)(agent => Future { processState(stateControl, agent) })
      .run()
  }

  private def decodeAndAssignState(gn: GraphNode, state: Option[GraphNode], simulationContext: Context): Agent = {
    val decoder = simulationContext.agentTypes(gn.label)

    val agent = gn.as(decoder)
    if (state.isDefined && agent.isInstanceOf[StatefulAgent]) {
      agent.asInstanceOf[StatefulAgent].setActiveState(state.get)
    }
    agent
  }

  private def processState(stateControl: DistributeStateControl, agent: Agent): Unit = {
    agent match {
      case statefulAgent: StatefulAgent => stateControl.executeFor(statefulAgent)
      case _                            =>
    }
  }

}

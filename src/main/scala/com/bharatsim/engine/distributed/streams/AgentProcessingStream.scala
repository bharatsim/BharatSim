package com.bharatsim.engine.distributed.streams

import akka.Done
import akka.actor.typed.ActorSystem
import akka.stream.scaladsl.Source
import com.bharatsim.engine.execution.AgentExecutor
import com.bharatsim.engine.execution.control.{BehaviourControl, StateControl}
import com.bharatsim.engine.graph.GraphNode
import com.bharatsim.engine.graph.GraphProvider.NodeId
import com.bharatsim.engine.graph.neo4j.BatchWriteNeo4jProvider
import com.bharatsim.engine.models.{Agent, StatefulAgent}
import com.bharatsim.engine.{ApplicationConfigFactory, Context}

import scala.concurrent.{ExecutionContextExecutor, Future}

class AgentProcessingStream(label: String, agentExecutor: AgentExecutor, simulationContext: Context)(implicit
    val system: ActorSystem[_]
) {

  implicit val ec: ExecutionContextExecutor = system.executionContext
  private val config = ApplicationConfigFactory.config
  private val bc = new BehaviourControl(simulationContext)
  private val sc = new StateControl(simulationContext)

  def fetch(agentId: NodeId): Future[Agent] =
    Future {
      val gn = simulationContext.graphProvider
        .asInstanceOf[BatchWriteNeo4jProvider]
        .fetchById(agentId)
        .get
      val decoder = simulationContext.agentTypes(label)
      gn.as(decoder)
    }

  def decode(gn: GraphNode): Future[Agent] =
    Future {
      val decoder = simulationContext.agentTypes(label)
      gn.as(decoder)
    }

  def decodeAndAssignState(gn: GraphNode, state: Option[GraphNode]): Future[Agent] =
    Future {
      val decoder = simulationContext.agentTypes(label)
      val agent = gn.as(decoder)
      if (state.isDefined) {
        agent match {
          case agent: StatefulAgent => agent.setActiveState(state.get)
          case _                    =>
        }
      }
      agent
    }

  def processBehaviour(a: Agent): Future[Agent] =
    Future {
      bc.executeFor(a)
      a
    }

  def processState(a: Agent): Future[Unit] =
    Future({
      a match {
        case agent: StatefulAgent => sc.executeFor(agent)
        case _                    =>
      }
    })

  def start(nodeIds: List[NodeId]): Future[Done] = {
    Source(nodeIds)
      .mapAsyncUnordered(config.nodeFetchBatchSize)(i => fetch(i))
      .mapAsyncUnordered(config.processBatchSize)(processBehaviour)
      .mapAsyncUnordered(config.processBatchSize)(processState)
      .run()
  }

  def startWithNodes(nodes: Iterable[GraphNode]): Future[Done] = {
    Source(nodes.toList)
      .mapAsyncUnordered(config.nodeFetchBatchSize)(i => decode(i))
      .mapAsyncUnordered(config.processBatchSize)(processBehaviour)
      .mapAsyncUnordered(config.processBatchSize)(processState)
      .run()
  }

  def startWithState(nodes: Iterable[(GraphNode, Option[GraphNode])]): Future[Done] = {
    Source(nodes.toList)
      .mapAsyncUnordered(config.nodeFetchBatchSize)(i => decodeAndAssignState(i._1, i._2))
      .mapAsyncUnordered(config.processBatchSize)(processBehaviour)
      .mapAsyncUnordered(config.processBatchSize)(processState)
      .run()
  }
}

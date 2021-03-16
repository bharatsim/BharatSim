package com.bharatsim.engine.distributed.streams

import akka.Done
import akka.actor.typed.ActorSystem
import akka.stream.scaladsl.Source
import com.bharatsim.engine.execution.{AgentExecutor, NodeWithDecoder}
import com.bharatsim.engine.graph.GraphNode
import com.bharatsim.engine.graph.GraphProvider.NodeId
import com.bharatsim.engine.graph.neo4j.BatchWriteNeo4jProvider
import com.bharatsim.engine.{ApplicationConfigFactory, Context}

import scala.concurrent.{ExecutionContextExecutor, Future}

class AgentProcessingStream(label: String, agentExecutor: AgentExecutor, simulationContext: Context)(implicit
    val system: ActorSystem[_]
) {

  implicit val ec: ExecutionContextExecutor = system.executionContext
  private val config = ApplicationConfigFactory.config

  def fetch(agentId: NodeId): Future[GraphNode] =
    Future {
      simulationContext.graphProvider
        .asInstanceOf[BatchWriteNeo4jProvider]
        .fetchById(agentId)
        .get
    }

  def processGraphNode(a: GraphNode): Future[Unit] = {
    val decoder = simulationContext.agentTypes(label)
    val nodeWithDecoder = NodeWithDecoder(a, decoder)
    Future {
      agentExecutor.execute(nodeWithDecoder)
    }
  }

  def start(nodeIds: List[NodeId]): Future[Done] = {
    Source(nodeIds)
      .mapAsyncUnordered(config.nodeFetchBatchSize)(i => fetch(i))
      .mapAsyncUnordered(config.processBatchSize)(processGraphNode)
      .run()
  }
}

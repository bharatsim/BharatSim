package com.bharatsim.engine.distributed.streams

import akka.Done
import akka.actor.typed.ActorSystem
import akka.stream.scaladsl.Source
import com.bharatsim.engine.ApplicationConfigFactory.config.{preProcessGroupCount, queryGroupSize}
import com.bharatsim.engine.graph.neo4j.queryBatching.{BatchQuery, GroupQuery, QueryWithPromise}
import org.neo4j.driver.Driver

import scala.concurrent.{ExecutionContext, Future}

class WriteOperationsStream(neo4jConnection: Driver)(implicit actorSystem: ActorSystem[_]) {
  private implicit val ec: ExecutionContext = actorSystem.executionContext

  def write(operations: List[QueryWithPromise]): Future[Done] = {

    Source(operations)
      .grouped(queryGroupSize)
      .mapAsync(preProcessGroupCount)(group => Future(BatchQuery(group).prepare()))
      .runForeach(list =>
        list.foreach(x => {
          val s = neo4jConnection.session()
          s.run(x._1, x._2)
          s.close()
        })
      )
  }
}

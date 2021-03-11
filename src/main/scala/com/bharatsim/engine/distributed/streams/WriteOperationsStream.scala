package com.bharatsim.engine.distributed.streams

import akka.Done
import akka.actor.typed.ActorSystem
import akka.stream.scaladsl.Source
import com.bharatsim.engine.distributed.store.WriteHandler.WriteQuery

import scala.concurrent.{ExecutionContext, Future}

class WriteOperationsStream(writeParallelism: Int)(implicit actorSystem: ActorSystem[_]) {
  private implicit val ec: ExecutionContext = actorSystem.executionContext

  def write(operations: List[WriteQuery], executor: WriteQuery => Unit): Future[Done] = {
    Source(operations)
      .map(q => executor(q))
      .run()
  }
}

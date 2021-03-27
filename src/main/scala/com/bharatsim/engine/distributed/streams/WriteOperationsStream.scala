package com.bharatsim.engine.distributed.streams

import akka.Done
import akka.actor.typed.ActorSystem
import akka.stream.scaladsl.Source
import com.bharatsim.engine.ApplicationConfigFactory.config.{preProcessGroupCount, queryGroupSize}
import com.bharatsim.engine.graph.neo4j.queryBatching.{BatchQuery, QueryWithPromise}
import org.neo4j.driver.internal.InternalRecord
import org.neo4j.driver.{Bookmark, Driver, Record, Result}
import org.neo4j.driver.SessionConfig.builder

import scala.concurrent.{ExecutionContext, Future, Promise}

class WriteOperationsStream(neo4jConnection: Driver)(implicit actorSystem: ActorSystem[_]) {
  private implicit val ec: ExecutionContext = actorSystem.executionContext

  def write(operations: List[QueryWithPromise], bookmark: Option[Bookmark] = None): Future[Bookmark] = {

    val s =
      if (bookmark.isDefined)
        neo4jConnection.session(builder().withBookmarks(bookmark.get).build())
      else neo4jConnection.session()

    Source(operations)
      .grouped(queryGroupSize)
      .mapAsync(preProcessGroupCount)(group => Future(BatchQuery(group).prepare()))
      .flatMapConcat(list =>
        Source(
          list
            .map(x => {
              val result = s.run(x._1, x._2).list()
              (result, x._3)
            })
            .toList
        )
      )
      .runForeach {
        case (records: java.util.List[Record], promises: Iterable[Promise[Record]]) =>
          val result = records.iterator
          promises.foreach(p => {
            if (result.hasNext)
              p.success(result.next())
            else {
              p.success(new InternalRecord(java.util.List.of[String](), Array()))
            }
          })
      }
      .map { _ =>
        val bookmark = s.lastBookmark()
        s.close()
        bookmark
      }

  }
}

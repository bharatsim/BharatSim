package com.bharatsim.engine.graph.neo4j.queryBatching

import java.util.Date

import akka.actor.typed.ActorSystem
import akka.stream.scaladsl.Source
import com.bharatsim.engine.ApplicationConfigFactory.config
import org.neo4j.driver.SessionConfig.builder
import org.neo4j.driver.internal.InternalRecord
import org.neo4j.driver.{Bookmark, Driver}

import scala.concurrent.{ExecutionContext, Future}

class WriteOperationsStream(neo4jConnection: Driver)(implicit actorSystem: ActorSystem[_]) {
  private implicit val executionContext: ExecutionContext = actorSystem.executionContext

  def write(operations: List[QueryWithPromise], bookmark: Option[Bookmark] = None): Future[Bookmark] = {

    val session =
      if (bookmark.isDefined)
        neo4jConnection.session(builder().withBookmarks(bookmark.get).build())
      else neo4jConnection.session()

    Source(operations)
      .grouped(config.queryGroupSize)
      .mapAsync(config.preProcessGroupCount)(group => Future(OrderedGroup(group).prepare()))
      .flatMapConcat(list =>
        Source(
          list
            .map(groupedQuery => {
              val st = new Date().getTime
              val result = session.writeTransaction((tx) => { tx.run(groupedQuery.query, groupedQuery.props).list() })
              val et = new Date().getTime
              GroupedQueryResult(result, groupedQuery, et - st)
            })
            .toList
        )
      )
      .runForeach {
        case gqResult: GroupedQueryResult =>
          val records = gqResult.records
          val promises = gqResult.groupedQuery.promises
          val result = records.iterator
          promises.foreach(promise => {
            if (result.hasNext)
              promise.success(result.next())
            else {
              promise.success(new InternalRecord(java.util.List.of[String](), Array()))
            }
          })
      }
      .map { _ =>
        val bookmark = session.lastBookmark()
        session.close()
        bookmark
      }

  }
}

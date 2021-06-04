package com.bharatsim.engine.graph.neo4j.queryBatching

import akka.actor.typed.ActorSystem
import akka.stream.scaladsl.Source
import com.bharatsim.engine.ApplicationConfig
import com.bharatsim.engine.ApplicationConfigFactory
import org.neo4j.driver.SessionConfig.builder
import org.neo4j.driver.internal.InternalRecord
import org.neo4j.driver.{AccessMode, Bookmark, Driver}

import scala.concurrent.{ExecutionContext, Future}

class WriteOperationsStream(neo4jConnection: Driver, config: ApplicationConfig = ApplicationConfigFactory.config)(
    implicit actorSystem: ActorSystem[_]
) {
  private implicit val executionContext: ExecutionContext = actorSystem.executionContext
  val retryableTransaction = new RetryableTransaction(config.maxQueryRetry)

  def write(operations: List[QueryWithPromise], bookmark: Option[Bookmark] = None): Future[Bookmark] = {

    val session =
      if (bookmark.isDefined)
        neo4jConnection.session(builder().withBookmarks(bookmark.get).build())
      else neo4jConnection.session()

    Source(operations)
      .grouped(config.writeBatchSize)
      .mapAsync(config.writeParallelism)(group => Future(OrderedGroup(group).prepare()))
      .flatMapConcat(list =>
        Source(
          list
            .map(groupedQuery => {
              retryableTransaction.run(groupedQuery, session, AccessMode.WRITE)
            })
            .toList
        )
      )
      .runForeach {
        case gqResult: GroupedQueryRecords =>
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
        case gqError: GroupedQueryError =>
          val promises = gqError.groupedQuery.promises
          promises.foreach(promise => { promise.failure(gqError.error) })
          throw gqError.error // write promises are not awaited, need to throw explicitly
      }
      .map { _ =>
        val bookmark = session.lastBookmark()
        session.close()
        bookmark
      }

  }
}

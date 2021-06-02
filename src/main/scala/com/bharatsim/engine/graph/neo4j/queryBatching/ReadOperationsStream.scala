package com.bharatsim.engine.graph.neo4j.queryBatching

import java.util.Date

import akka.actor.typed.{ActorSystem, DispatcherSelector}
import akka.stream.scaladsl.{Keep, Sink, Source}
import akka.stream.{OverflowStrategy, QueueOfferResult}
import com.bharatsim.engine.{ApplicationConfig, ApplicationConfigFactory}
import com.typesafe.scalalogging.LazyLogging
import org.neo4j.driver.SessionConfig.builder
import org.neo4j.driver._
import org.neo4j.driver.internal.InternalRecord

import scala.concurrent.duration.{Duration, DurationInt}
import scala.concurrent.{Await, ExecutionContext, Future}
import scala.jdk.CollectionConverters.IterableHasAsJava
import scala.util.{Failure, Success}

class ReadOperationsStream(neo4jConnection: Driver, config: ApplicationConfig = ApplicationConfigFactory.config)(
    implicit actorSystem: ActorSystem[_]
) extends LazyLogging {
  private implicit val ec: ExecutionContext = actorSystem.dispatchers.lookup(DispatcherSelector.blocking())
  private var sessionConfig = builder().withDefaultAccessMode(AccessMode.READ).build()
  val retryableTransaction = new RetryableTransaction(config.maxQueryRetry)

  def setBookMarks(bookmarks: List[Bookmark]) = {
    logger.debug("setting bookmarks {}", bookmarks)
    this.sessionConfig = builder().withDefaultAccessMode(AccessMode.READ).withBookmarks(bookmarks.asJava).build()
  }
  private val sourceQueue = Source
    .queue[QueryWithPromise](0, OverflowStrategy.fail, 1)
    .groupedWithin(config.readBatchSize, config.readWaitTime.millisecond)
    .mapAsyncUnordered(config.readParallelism)(group => Future { UnorderedGroup(group).prepare() })
    .flatMapConcat(groupedQueries => Source(groupedQueries.toList))
    .mapAsyncUnordered(config.readParallelism)(gq =>
      Future {
        val session = neo4jConnection.session(sessionConfig)
        val result = retryableTransaction.run(gq, session, AccessMode.READ)
        session.close()
        result
      }
    )
    .toMat(Sink.foreachAsync(config.readParallelism)((gqResult: GroupedQueryResult) => {
      Future {
        gqResult match {
          case records: GroupedQueryRecords => mapRecords(records)
          case error: GroupedQueryError     => mapErrors(error)
        }
      }
    }))(Keep.left)
    .run()

  sourceQueue
    .watchCompletion()
    .onComplete({
      case Success(value)     => logger.debug("Read stream completed {}", value)
      case Failure(exception) => logger.error("Read steam failed {}", exception.toString)
    })

  private def mapRecords(gqResult: GroupedQueryRecords) = {
    val records = gqResult.records
    val gq = gqResult.groupedQuery
    val promises = gq.promises
    logger.debug("Read finished {} in time {} ms ", records.size, gqResult.time)
    val result = records.iterator
    promises.foreach(p => {
      if (result.hasNext) {
        p.success(result.next())
      } else {
        logger.warn("GroupQueryResult: number of records is less than number of queries")
        p.success(new InternalRecord(java.util.List.of[String](), Array()))
      }
    })
  }
  private def mapErrors(gqResult: GroupedQueryError) = {
    val gq = gqResult.groupedQuery
    val promises = gq.promises
    promises.foreach(p => { p.failure(gqResult.error) })
  }

  def enqueue(query: QueryWithPromise) = {
    val st = new Date().getTime
    val eventualOfferResult = sourceQueue.offer(query)
    try {
      val result = Await.result(eventualOfferResult, Duration.Inf)
      val diff = new Date().getTime - st
      val expectedEnqueueThreshold = 500
      if (result == QueueOfferResult.enqueued && diff > expectedEnqueueThreshold) {
        logger.warn("Enqueue took long time {}", diff)
      }
      if (result != QueueOfferResult.enqueued) {
        logger.error("Enqueue failed : {} ", result)
        throw new EnqueueFailedException(s"${result}")
      }
    } catch {
      case exception: Throwable =>
        logger.error("Enqueue failed : {} ", exception.toString)
        throw new EnqueueFailedException(exception.toString)
    }
  }

  def close(): Unit = {
    logger.debug("Closing read queue")
    sourceQueue.complete()
  }
}

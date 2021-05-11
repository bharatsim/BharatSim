package com.bharatsim.engine.graph.neo4j.queryBatching

import java.time
import java.util.Date

import akka.actor.typed.{ActorSystem, DispatcherSelector}
import akka.stream.scaladsl.{Keep, Sink, Source}
import akka.stream.{OverflowStrategy, QueueOfferResult}
import com.bharatsim.engine.{ApplicationConfig, ApplicationConfigFactory}
import com.typesafe.scalalogging.LazyLogging
import org.neo4j.driver.SessionConfig.builder
import org.neo4j.driver._
import org.neo4j.driver.exceptions.ClientException
import org.neo4j.driver.internal.InternalRecord

import scala.annotation.tailrec
import scala.concurrent.duration.{Duration, DurationInt}
import scala.concurrent.{Await, ExecutionContext, Future}
import scala.jdk.CollectionConverters.IterableHasAsJava
import scala.util.{Failure, Success}

class ReadOperationsStream(val neo4jConnection: Driver)(implicit actorSystem: ActorSystem[_]) extends LazyLogging {
  private implicit val ec: ExecutionContext = actorSystem.dispatchers.lookup(DispatcherSelector.blocking())

  private val config: ApplicationConfig = ApplicationConfigFactory.config

  private var sessionConfig = builder().withDefaultAccessMode(AccessMode.READ).build()

  def setBookMarks(bookmarks: List[Bookmark]) = {
    logger.info("setting bookmarks {}", bookmarks)
    this.sessionConfig = builder().withDefaultAccessMode(AccessMode.READ).withBookmarks(bookmarks.asJava).build()
  }

  private val sourceQueue = Source
    .queue[QueryWithPromise](config.processBatchSize * 2, OverflowStrategy.backpressure, config.processBatchSize)
    .groupedWithin(config.readBatchSize, config.readWaitTime.millisecond)
    .mapAsyncUnordered(config.readParallelism)(group => Future { UnorderedGroup(group).prepareGroups() })
    .flatMapConcat(groupedQueries => Source(groupedQueries.toList))
    .mapAsyncUnordered(config.readParallelism)(gq => {
      Future {
        try {
          val session = neo4jConnection.session(sessionConfig)
          val st = new Date().getTime

          val resultList = session.readTransaction((tx: Transaction) => {
            val res = tx.run(gq.query, gq.props)
            res.list();
          })
          val et = new Date().getTime
          session.close()
          GroupedQueryResult(resultList, gq, et - st)
        } catch {
          case ex: Throwable =>
            logger.info("failed query {} with ex{}", gq.query, ex)
            throw ex;
        }
      }
    })
    .toMat(Sink.foreachAsync(config.readParallelism)((gqResult: GroupedQueryResult) => {
      Future {
        val records = gqResult.records
        val gq = gqResult.groupedQuery
        val promises = gq.promises
        val query = gq.query
        val props = gq.props.get("propsList").asInstanceOf[java.util.Collection[Object]]
        logger.info("Read finished {} in time {} ms ", records.size, gqResult.time)
        val result = records.iterator
        promises.foreach(p => {
          try {
            if (result.hasNext) {
              p.success(result.next())
            } else {
              logger.info("got No results ")
              p.success(new InternalRecord(java.util.List.of[String](), Array()))
            }
          } catch {
            case ex: Throwable => logger.info("Failed assinging promis")
          }
        })
      }
    }))(Keep.left)
    .run()

  sourceQueue
    .watchCompletion()
    .onComplete({
      case Success(value) =>
        logger.info("read stream completed", value)
      case Failure(exception) => logger.info("read steam failed")
    })

  def enqueue(query: QueryWithPromise): Long = {

    val st = new Date().getTime
    val eventualOfferResult = sourceQueue.offer(query)

    eventualOfferResult.onComplete({
      case Success(value)     => if (value != QueueOfferResult.enqueued) logger.info("success enqueue {}", value)
      case Failure(exception) => logger.info("failed to enqueue")
    })

    val result = Await.result(eventualOfferResult, Duration.Inf)

    val diff = new Date().getTime - st
    if (diff > 500) {
      logger.info(" enqueue took long time {}", diff)
    }

    if (result != QueueOfferResult.enqueued) {
      logger.info("success enqueue {}", result)
    }
    diff
  }

}

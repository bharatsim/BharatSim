package com.bharatsim.engine.distributed.streams

import java.util
import java.util.Date

import akka.NotUsed
import akka.actor.typed.{ActorSystem, DispatcherSelector}
import akka.stream.scaladsl.{Keep, Sink, Source}
import akka.stream.{OverflowStrategy, QueueOfferResult}
import com.bharatsim.engine.graph.neo4j.queryBatching.{GroupQuery, QueryWithPromise}
import com.bharatsim.engine.{ApplicationConfig, ApplicationConfigFactory}
import com.typesafe.scalalogging.LazyLogging
import org.neo4j.driver.internal.InternalRecord
import org.neo4j.driver.{Driver, Record}

import scala.collection.mutable
import scala.collection.mutable.ListBuffer
import scala.concurrent.duration.{Duration, DurationInt}
import scala.concurrent.{Await, ExecutionContext, Future, Promise}
import scala.jdk.CollectionConverters.IterableHasAsJava
import scala.util.{Failure, Success}

class ReadOperationsStream(val neo4jConnection: Driver)(implicit actorSystem: ActorSystem[_]) extends LazyLogging {
  private implicit val ec: ExecutionContext =
    actorSystem.dispatchers.lookup(DispatcherSelector.fromConfig("dispatchers.data-store-blocking"))

  private val config: ApplicationConfig = ApplicationConfigFactory.config

  private val sourceQueue = Source
    .queue[QueryWithPromise](config.processBatchSize * 2, OverflowStrategy.backpressure, config.processBatchSize)
    .groupedWithin(Int.MaxValue, config.readWaitTime.millisecond)
    .mapAsyncUnordered(config.readParallelism)(group => Future { GroupUnOrdered(group).prepareGroups() })
    .mapAsyncUnordered(config.readParallelism)(groupedQueries => {
      Future {
        val resultList = groupedQueries
          .map(gq => {
            try {
              val s = neo4jConnection.session()
              val result = s.run(gq.query, gq.props).list()
              s.close()
              GQResult(result, gq)
            } catch {
              case ex: Throwable =>
                logger.info("failed query {} with ex{}", gq.query, ex)
                throw ex;
            }
          })
          .toList

        GQResultSource(Source(resultList), resultList.size)
      }
    })
    .toMat(Sink.foreachAsync(config.readParallelism)((sourceResult: GQResultSource) => {
      Future {
        sourceResult.source.runWith(Sink.foreachAsync(sourceResult.size)((gqResult: GQResult) => {
          Future {
            val records = gqResult.records
            val gq = gqResult.gq
            val promises = gq.promises
            val query = gq.query
            val props = gq.props.get("propsList").asInstanceOf[java.util.Collection[Object]]
            logger.info("Read finished {} for promise size {} for Id {} ", records.size, promises.size)
            val result = records.iterator
            promises.foreach(p => {
              try {
                if (result.hasNext) {
                  p.success(result.next())
                } else {
                  logger.info("got No results ")
                  //            throw new Error("no result")
                  //            System.exit(1)
                  p.success(new InternalRecord(java.util.List.of[String](), Array()))
                }
              } catch {
                case ex: Throwable => logger.info("Failed assinging promis")
              }
            })
          }
        }))
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
    val p = sourceQueue.offer(query)
//
    p.onComplete({
      case Success(value)     => if (value != QueueOfferResult.enqueued) logger.info("success enqueue {}", value)
      case Failure(exception) => logger.info("failed to enqueue")
    })

    val result = Await.result(p, Duration.Inf)

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

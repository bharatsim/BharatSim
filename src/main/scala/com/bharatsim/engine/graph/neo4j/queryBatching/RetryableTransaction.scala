package com.bharatsim.engine.graph.neo4j.queryBatching

import java.util.Date

import com.typesafe.scalalogging.LazyLogging
import org.neo4j.driver.exceptions.Neo4jException
import org.neo4j.driver.{AccessMode, Session, Transaction}

class RetryableTransaction(maxRetry: Int) extends LazyLogging {

  private def runWithRetry(
      gq: GroupedQuery,
      session: Session,
      mode: AccessMode,
      retryAttempt: Int = 1
  ): GroupedQueryResult = {
    try {

      def transactionWork(tx: Transaction) = {
        val res = tx.run(gq.query, gq.props)
        res.list();
      }
      val st = new Date().getTime

      val resultList =
        if (mode == AccessMode.READ) session.readTransaction(transactionWork)
        else session.writeTransaction(transactionWork)
      val et = new Date().getTime
      GroupedQueryRecords(resultList, gq, et - st)
    } catch {
      case neo4jException: Neo4jException =>
        if (retryAttempt >= maxRetry) {
          logger.error("Query Failed: {}, all retry attempt failed for query {}", neo4jException.toString, gq.query)
          GroupedQueryError(neo4jException, gq)
        } else {
          logger.warn("Query Failed: {}, on attempt: {}. Retrying...", neo4jException.toString, retryAttempt)
          runWithRetry(gq, session, mode, retryAttempt + 1)
        }
      case exception: Throwable =>
        logger.error("Query Failed: {}", exception.toString, gq.query)
        GroupedQueryError(exception, gq)
    }
  }

  def run(gq: GroupedQuery, session: Session, mode: AccessMode): GroupedQueryResult = {
    runWithRetry(gq, session, mode)
  }
}

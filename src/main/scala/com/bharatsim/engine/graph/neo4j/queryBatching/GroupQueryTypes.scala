package com.bharatsim.engine.graph.neo4j.queryBatching

import java.util

import org.neo4j.driver.Record

import scala.collection.mutable.ListBuffer
import scala.concurrent.Promise

case class SubstitutableQuery(
    queryBody: String,
    props: util.Map[String, java.lang.Object] = new util.HashMap[String, java.lang.Object]()
)

case class GroupQueryHolder(
    query: String,
    multiProps: ListBuffer[util.Map[String, java.lang.Object]],
    promises: ListBuffer[Promise[Record]]
)
case class QueryWithPromise(query: SubstitutableQuery, promise: Promise[Record])
case class GroupedQuery(query: String, props: util.Map[String, Object], promises: Iterable[Promise[Record]])
trait GroupedQueryResult
case class GroupedQueryRecords(records: util.List[Record], groupedQuery: GroupedQuery, time: Long)
    extends GroupedQueryResult
case class GroupedQueryError(error: Throwable, groupedQuery: GroupedQuery) extends GroupedQueryResult

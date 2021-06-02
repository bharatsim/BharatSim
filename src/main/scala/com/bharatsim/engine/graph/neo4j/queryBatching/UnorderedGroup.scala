package com.bharatsim.engine.graph.neo4j.queryBatching

import java.util

import com.typesafe.scalalogging.LazyLogging

import scala.collection.mutable
import scala.collection.mutable.ListBuffer
import scala.jdk.CollectionConverters.IterableHasAsJava

case class UnorderedGroup(queries: Iterable[QueryWithPromise], label: String = "Read") extends LazyLogging {
  private val groups = mutable.HashMap.empty[String, GroupQueryHolder]

  def prepare(): Iterable[GroupedQuery] = {
    queries.foreach(query => {
      val groupQuery =
        groups.getOrElseUpdate(
          query.query.queryBody,
          GroupQueryHolder(query.query.queryBody, ListBuffer.empty, ListBuffer.empty)
        )
      groupQuery.multiProps.addOne(query.query.props)
      groupQuery.promises.addOne(query.promise)
    })
    val groupedQueries = groups.values.map(queryHolder => {
      val unwindStatement = "UNWIND $propsList as props with props, randomUUID() as uuid\n"
      val listProps = new util.HashMap[String, Object]
      listProps.put("propsList", queryHolder.multiProps.asJava)
      GroupedQuery(s"$unwindStatement${queryHolder.query}", listProps, queryHolder.promises)
    })

    logger.debug("{} grouped {} into {}", label, queries.size, groupedQueries.size)
    groupedQueries
  }
}

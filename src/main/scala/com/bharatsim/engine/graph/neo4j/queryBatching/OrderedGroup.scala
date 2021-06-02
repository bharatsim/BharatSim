package com.bharatsim.engine.graph.neo4j.queryBatching

import java.util

import com.typesafe.scalalogging.LazyLogging

import scala.collection.mutable.ListBuffer
import scala.jdk.CollectionConverters.IterableHasAsJava

case class OrderedGroup(queries: Iterable[QueryWithPromise], label: String = "Write") extends LazyLogging {

  def prepare(): Iterable[GroupedQuery] = {
    val orderedGroups = ListBuffer.empty[GroupQueryHolder]

    queries
      .foreach(qp => {
        val q = qp.query
        val p = qp.promise
        if (orderedGroups.nonEmpty && orderedGroups.last.query == q.queryBody) {
          val lastQuery = orderedGroups.last
          lastQuery.multiProps.addOne(q.props)
          lastQuery.promises.addOne(p)

        } else {
          orderedGroups.addOne(GroupQueryHolder(q.queryBody, ListBuffer(q.props), ListBuffer(p)))
        }
      })

    val groupedQueries = orderedGroups
      .map(gq => {
        val unwindStatement = "UNWIND $propsList as props\n"
        val listProps = new util.HashMap[String, Object]
        listProps.put("propsList", gq.multiProps.asJava)
        GroupedQuery(s"$unwindStatement${gq.query}", listProps, gq.promises)
      })

    logger.debug("{} grouped {} into {}", label, queries.size, groupedQueries.size)
    groupedQueries
  }
}

package com.bharatsim.engine.graph.neo4j.queryBatching

import java.util

import com.typesafe.scalalogging.LazyLogging
import org.neo4j.driver.Record

import scala.collection.mutable.ListBuffer
import scala.concurrent.Promise
import scala.jdk.CollectionConverters.IterableHasAsJava

case class QueryWithPromise(b: SubstituableQuery, p: Promise[Record])

case class BatchQuery(queries: Seq[QueryWithPromise]) extends LazyLogging {
  def formSingleQuery(): ListBuffer[(String, util.Map[String, java.lang.Object], Iterable[Promise[Record]])] = {
    val orderedGroups = ListBuffer.empty[GroupQuery]

    queries
      .foreach(qp => {
        val q = qp.b
        val p = qp.p
        if (orderedGroups.nonEmpty && orderedGroups.last.query == q.queryBody) {
          val lastQuery = orderedGroups.last
          lastQuery.multiProps.addOne(q.props)
          lastQuery.promises.addOne(p)

        } else {
          orderedGroups.addOne(GroupQuery(q.queryBody, ListBuffer(q.props), ListBuffer(p)))
        }
      })

    orderedGroups
      .map(gq => {
        val unwindStatement = "UNWIND $propsList as props\n"
        val listProps = new util.HashMap[String, Object]
        listProps.put("propsList", gq.multiProps.asJava)
        (s"$unwindStatement${gq.query}", listProps, gq.promises)
      })
  }

  def prepare(): Iterable[(String, util.Map[String, Object], Iterable[Promise[Record]])] = {
    val ret = formSingleQuery()
    logger.info("Grouped into {} queries", ret.size)
    ret
  }
}

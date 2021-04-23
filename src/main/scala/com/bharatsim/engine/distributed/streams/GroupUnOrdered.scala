package com.bharatsim.engine.distributed.streams

import java.util

import akka.NotUsed
import akka.stream.scaladsl.Source
import com.bharatsim.engine.graph.neo4j.queryBatching.{GroupQuery, QueryWithPromise}
import com.typesafe.scalalogging.LazyLogging
import org.neo4j.driver.Record
import org.neo4j.driver.summary.ResultSummary

import scala.collection.mutable
import scala.collection.mutable.ListBuffer
import scala.concurrent.Promise
import scala.jdk.CollectionConverters.IterableHasAsJava

case class GQ(query: String, props: util.Map[String, Object], promises: Iterable[Promise[Record]])
case class GQResult(records: util.List[Record], gq: GQ, time: Long)
case class GQResultSource(source: Source[GQResult, NotUsed], size: Int)

case class GroupUnOrdered(queries: Iterable[QueryWithPromise], label: String = "Read") extends LazyLogging {
  private val groups = mutable.HashMap.empty[String, GroupQuery]

  def prepareGroups(): Iterable[GQ] = {
    logger.info("grouping started for size {}", queries.size)
    queries.foreach(q => {
      val groupQuery =
        groups.getOrElseUpdate(
          q.b.queryBody,
          GroupQuery(q.b.queryBody, ListBuffer.empty, ListBuffer.empty)
        )
      groupQuery.multiProps.addOne(q.b.props)
      groupQuery.promises.addOne(q.p)
    })
    val res = groups.values.map(gq => {
      val unwindStatement = "UNWIND $propsList as props with props, randomUUID() as uuid\n"
      val listProps = new util.HashMap[String, Object]
      listProps.put("propsList", gq.multiProps.asJava)
      GQ(s"$unwindStatement${gq.query}", listProps, gq.promises)
    })

    logger.info("{} grouped {} into {}", label, queries.size, res.size)
    res
  }
}

package com.bharatsim.engine.graph.neo4j.queryBatching

import java.util

import com.typesafe.scalalogging.LazyLogging
import org.neo4j.driver.Result

import scala.collection.mutable.ListBuffer
import scala.concurrent.Promise
import scala.jdk.CollectionConverters.IterableHasAsJava

case class QueryWithPromise(b: SubstituableQuery, p: Promise[Result])

case class BatchQuery(queries: Seq[QueryWithPromise]) extends LazyLogging {
  def generate(
      generator: ParamNameGenerator,
      count: Int,
      list: List[String] = List.empty
  ): List[String] = {
    if (count == 0) list
    else {
      val name = generator.get
      generate(generator.next, count - 1, name :: list)
    }
  }

  def replaceParams(
      generator: ParamNameGenerator,
      body: SubstitutableString
  ): String = {
    body match {
      case SingleParamString(bodyApply) =>
        val names = generate(generator, 1)
        bodyApply(names.head)
      case TwoParamString(bodyApply) =>
        val names = generate(generator, 2)
        bodyApply(names.head, names(1))
      case ThreeParamString(bodyApply) =>
        val names = generate(generator, 3)
        bodyApply(names.head, names(1), names(2))
      case FourParamString(bodyApply) =>
        val names = generate(generator, 4)
        bodyApply(names.head, names(1), names(2), names(3))
      case FiveParamString(bodyApply) =>
        val names = generate(generator, 5)
        bodyApply(names.head, names(1), names(2), names(3), names(4))
      case _ => throw new Exception("Batchable query must have same type of body and return substitutable strings")
    }
  }

  def formSingleQuery(): ListBuffer[(String, util.Map[String, java.lang.Object])] = {
    val orderedGroups = ListBuffer.empty[GroupQuery]

    queries
      .map(q => q.b)
      .foreach(q => {
        if (orderedGroups.nonEmpty && orderedGroups.last.query == q.queryBody) {
          orderedGroups.last.multiProps.addOne(q.props)
        } else {
          orderedGroups.addOne(GroupQuery(q.queryBody, ListBuffer(q.props)))
        }
      })

    orderedGroups
      .map(gq => {
        val replacedParamQuery = replaceParams(ParamNameGenerator(), gq.query)
        val unwindStatement = "UNWIND $propsList as props\n"
        val listProps = new util.HashMap[String, Object]
        listProps.put("propsList", gq.multiProps.asJava)
        (s"$unwindStatement$replacedParamQuery", listProps)
      })
  }

  def prepare(): Iterable[(String, util.Map[String, Object])] = {
    val ret = formSingleQuery()
    logger.info("Grouped into {} queries", ret.size)
    ret
  }
}

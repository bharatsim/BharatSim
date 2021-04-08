package com.bharatsim.engine.graph.neo4j.queryBatching

import java.util

import org.neo4j.driver.Record

import scala.collection.mutable.ListBuffer
import scala.concurrent.Promise

case class SubstituableQuery(
    queryBody: String,
    props: util.Map[String, java.lang.Object] = new util.HashMap[String, java.lang.Object]()
)

case class GroupQuery(
    query: String,
    multiProps: ListBuffer[util.Map[String, java.lang.Object]],
    promises: ListBuffer[Promise[Record]]
)

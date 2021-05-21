package com.bharatsim.engine.graph.neo4j.queryBatching

import org.scalatest.funsuite.AnyFunSuite
import org.scalatest.matchers.should.Matchers

import scala.concurrent.Promise
import scala.jdk.CollectionConverters.{IterableHasAsJava, MapHasAsJava}

class UnorderedGroupTest extends AnyFunSuite with Matchers {

  test("should prepare unOrdered groups of queries") {
    val unwindQuery = "UNWIND $propsList as props with props, randomUUID() as uuid\n"
    val propList = "propsList"
    val createQuery = "CREATE QUERY"
    val updateQuery = "UPDATE QUERY"
    val query1 = QueryWithPromise(SubstitutableQuery(createQuery, Map("q1" -> new Object).asJava), Promise())
    val query2 = QueryWithPromise(SubstitutableQuery(createQuery, Map("q2" -> new Object).asJava), Promise())
    val query3 = QueryWithPromise(SubstitutableQuery(updateQuery, Map("q3" -> new Object).asJava), Promise())
    val query4 = QueryWithPromise(SubstitutableQuery(updateQuery, Map("q4" -> new Object).asJava), Promise())
    val query5 = QueryWithPromise(SubstitutableQuery(createQuery, Map("q5" -> new Object).asJava), Promise())
    val query6 = QueryWithPromise(SubstitutableQuery(updateQuery, Map("q6" -> new Object).asJava), Promise())

    val groupedQueries = UnorderedGroup(List(query1, query2, query3, query4, query5, query6)).prepare().toList

    groupedQueries should have size 2
    val group1 = groupedQueries.head
    val group2 = groupedQueries.tail.head
    group1.query shouldBe unwindQuery + createQuery
    group1.props.get(propList) shouldBe List(query1.query.props, query2.query.props, query5.query.props).asJava
    group1.promises shouldBe List(query1.promise, query2.promise, query5.promise)

    group2.query shouldBe unwindQuery + updateQuery
    group2.props.get(propList) shouldBe List(query3.query.props, query4.query.props, query6.query.props).asJava
    group2.promises shouldBe List(query3.promise, query4.promise, query6.promise)
  }
}

package com.bharatsim.engine.graph.neo4j.queryBatching

import org.mockito.{ArgumentMatchersSugar, MockitoSugar}
import org.neo4j.driver.exceptions.ClientException
import org.neo4j.driver.{AccessMode, Record, Session}
import org.scalatest.funsuite.AnyFunSuite
import org.scalatest.matchers.should.Matchers

import scala.jdk.CollectionConverters.MapHasAsJava

class RetryableTransactionTest extends AnyFunSuite with MockitoSugar with ArgumentMatchersSugar with Matchers {
  val maxRetry = 3

  test("should execute read transactions") {
    val mockSession = mock[Session]
    val groupedQuery = GroupedQuery("query", Map.empty[String, Object].asJava, List.empty)
    val records = java.util.List.of(mock[Record])
    when(mockSession.readTransaction[java.util.List[Record]](any)).thenReturn(records)
    val result =
      new RetryableTransaction(maxRetry)
        .run(groupedQuery, mockSession, AccessMode.READ)
        .asInstanceOf[GroupedQueryRecords]
    verify(mockSession).readTransaction(any)
    verify(mockSession, never).writeTransaction(any)
    result.groupedQuery shouldBe groupedQuery
    result.records shouldBe records
  }

  test("should execute write transactions") {
    val mockSession = mock[Session]
    val groupedQuery = GroupedQuery("query", Map.empty[String, Object].asJava, List.empty)
    val records = java.util.List.of(mock[Record])
    when(mockSession.writeTransaction[java.util.List[Record]](any)).thenReturn(records)
    val result =
      new RetryableTransaction(maxRetry)
        .run(groupedQuery, mockSession, AccessMode.WRITE)
        .asInstanceOf[GroupedQueryRecords]
    verify(mockSession, never).readTransaction(any)
    verify(mockSession).writeTransaction(any)
    result.groupedQuery shouldBe groupedQuery
    result.records shouldBe records
  }

  test("should retry transactions till max retry and return error when Neo4jException is thrown") {
    val mockSession = mock[Session]
    val groupedQuery = GroupedQuery("query", Map.empty[String, Object].asJava, List.empty)
    val exception = new ClientException("Error")
    when(mockSession.readTransaction[java.util.List[Record]](any)).thenThrow(exception)
    val result =
      new RetryableTransaction(maxRetry)
        .run(groupedQuery, mockSession, AccessMode.READ)
        .asInstanceOf[GroupedQueryError]
    verify(mockSession, times(maxRetry)).readTransaction(any)
    verify(mockSession, never).writeTransaction(any)
    result.groupedQuery shouldBe groupedQuery
    result.error shouldBe exception
  }

  test("should not retry transactions for Exceptions other than Neo4jException") {
    val mockSession = mock[Session]
    val groupedQuery = GroupedQuery("query", Map.empty[String, Object].asJava, List.empty)
    val exception = new Exception("Error")
    when(mockSession.readTransaction[java.util.List[Record]](any)).thenThrow(exception)
    val result =
      new RetryableTransaction(maxRetry)
        .run(groupedQuery, mockSession, AccessMode.READ)
        .asInstanceOf[GroupedQueryError]
    verify(mockSession).readTransaction(any)
    verify(mockSession, never).writeTransaction(any)
    result.groupedQuery shouldBe groupedQuery
    result.error shouldBe exception
  }
}

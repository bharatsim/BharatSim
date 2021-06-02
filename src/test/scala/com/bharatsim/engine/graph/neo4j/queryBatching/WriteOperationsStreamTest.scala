package com.bharatsim.engine.graph.neo4j.queryBatching

import akka.actor.typed.ActorSystem
import akka.actor.typed.scaladsl.Behaviors
import com.bharatsim.engine.ApplicationConfig
import com.dimafeng.testcontainers.{ForAllTestContainer, Neo4jContainer}
import org.mockito.MockitoSugar
import org.neo4j.driver.exceptions.ClientException
import org.neo4j.driver.{AuthTokens, Driver, GraphDatabase, Record}
import org.scalatest.BeforeAndAfterEach
import org.scalatest.funsuite.AnyFunSuite
import org.scalatest.matchers.should.Matchers

import scala.concurrent.duration.Duration
import scala.concurrent.{Await, Promise}
import scala.jdk.CollectionConverters.{IterableHasAsScala, MapHasAsJava}

class WriteOperationsStreamTest
    extends AnyFunSuite
    with BeforeAndAfterEach
    with Matchers
    with ForAllTestContainer
    with MockitoSugar {
  override val container = Neo4jContainer()
  val mockConfig = spy(new ApplicationConfig())

  var neo4jConnection: Driver = null;
  override def beforeEach(): Unit = {
    when(mockConfig.maxQueryRetry).thenReturn(1)
    val uri = container.boltUrl
    neo4jConnection = GraphDatabase.driver(uri, AuthTokens.basic(container.username, container.password))

  }

  override def afterEach(): Unit = {
    val session = neo4jConnection.session
    session.writeTransaction(tx => {
      tx.run("Match(n) detach delete n")
    })
    session.close()
  }

  test("should execute writes in order") {
    implicit val actorSystem = ActorSystem(Behaviors.empty, "WriteOperationTest")
    val label = "Person"
    val createQuery = s"CREATE (n:$label) SET n=props return id(n) as nodeId"
    val update = s"Match (n:$label) SET n.age=props.age"
    val query1 =
      QueryWithPromise(SubstitutableQuery(createQuery, Map("name" -> "N1".asInstanceOf[Object]).asJava), Promise())
    val query2 =
      QueryWithPromise(SubstitutableQuery(createQuery, Map("name" -> "N2".asInstanceOf[Object]).asJava), Promise())
    val query3 =
      QueryWithPromise(SubstitutableQuery(update, Map("age" -> 3.asInstanceOf[Object]).asJava), Promise())
    val eventualBookmark = new WriteOperationsStream(neo4jConnection, mockConfig).write(List(query1, query2, query3))
    Await.result(eventualBookmark, Duration.Inf)
    val actualId1 = Await.result(query1.promise.future, Duration.Inf).get("nodeId").asLong()
    val actualId2 = Await.result(query2.promise.future, Duration.Inf).get("nodeId").asLong()
    val query3Result = Await.result(query3.promise.future, Duration.Inf).asMap()

    val session = neo4jConnection.session()
    val persons: List[Record] = session
      .readTransaction(tx => {
        val result = tx.run(s"Match (n:$label) return properties(n) as props, id(n) as nodeId")
        result.list().asScala.toList
      })
    session.close()
    val props1 = persons.head.get("props").asMap()
    val props2 = persons.last.get("props").asMap()
    val id1 = persons.head.get("nodeId").asLong()
    val id2 = persons.last.get("nodeId").asLong()
    props1.get("name") shouldBe "N1"
    props1.get("age") shouldBe 3
    props2.get("name") shouldBe "N2"
    props2.get("age") shouldBe 3
    actualId1 shouldBe id1
    actualId2 shouldBe id2
    query3Result shouldBe empty
  }

  test("should fail promises and escalate on error") {
    implicit val actorSystem = ActorSystem(Behaviors.empty, "WriteOperationTest")
    val errorQuery = "ErrorQuery"
    val query1 =
      QueryWithPromise(SubstitutableQuery(errorQuery, Map("name" -> "N1".asInstanceOf[Object]).asJava), Promise())
    val query2 =
      QueryWithPromise(SubstitutableQuery(errorQuery, Map("name" -> "N2".asInstanceOf[Object]).asJava), Promise())

    val eventualBookmark = new WriteOperationsStream(neo4jConnection, mockConfig).write(List(query1, query2))
    the[ClientException] thrownBy Await.result(eventualBookmark, Duration.Inf)
    the[ClientException] thrownBy Await.result(query1.promise.future, Duration.Inf)
    the[ClientException] thrownBy Await.result(query2.promise.future, Duration.Inf)

  }

}

package com.bharatsim.engine.graph.neo4j.queryBatching

import akka.actor.typed.ActorSystem
import akka.actor.typed.scaladsl.Behaviors
import com.bharatsim.engine.ApplicationConfig
import com.dimafeng.testcontainers.{ForAllTestContainer, Neo4jContainer}
import org.mockito.MockitoSugar
import org.neo4j.driver.Values.parameters
import org.neo4j.driver.{AuthTokens, Driver, GraphDatabase}
import org.scalatest.BeforeAndAfterEach
import org.scalatest.funsuite.AnyFunSuite
import org.scalatest.matchers.should.Matchers
import com.typesafe.scalalogging.Logger
import org.neo4j.driver.exceptions.ClientException

import scala.concurrent.duration.Duration
import scala.concurrent.{Await, Promise}
import scala.jdk.CollectionConverters.MapHasAsJava

class ReadOperationsStreamTest
    extends AnyFunSuite
    with BeforeAndAfterEach
    with Matchers
    with ForAllTestContainer
    with MockitoSugar {
  override val container = Neo4jContainer()

  var neo4jConnection: Driver = null;
  val mockConfig = spy(new ApplicationConfig())
  implicit val actorSystem = ActorSystem(Behaviors.empty, "ReadOperationTest")

  override def beforeEach(): Unit = {
    when(mockConfig.maxQueryRetry).thenReturn(1)
    when(mockConfig.readWaitTime).thenReturn(5)
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

  test("execute read queries and map result to appropriate promise") {
    implicit val actorSystem = ActorSystem(Behaviors.empty, "ReadOperationTest")

    val personLabel = "Person"
    val employeeLabel = "Employee"
    val createQuery1 = s"CREATE (n:$personLabel) SET n={name:$$name,id:$$id} return id(n) as nodeId"
    val createQuery2 = s"CREATE (n:$employeeLabel) SET n={name:$$name, id:$$id} return id(n) as nodeId"
    val session = neo4jConnection.session()
    session.writeTransaction(tx => {
      tx.run(createQuery1, parameters("name", "P1", "id", "1"))
      tx.run(createQuery1, parameters("name", "P2", "id", "2"))
      tx.run(createQuery2, parameters("name", "E1", "id", "1"))
      tx.run(createQuery2, parameters("name", "E2", "id", "2"))
    })

    val findPerson = s"""OPTIONAL Match (n:$personLabel) where properties(n).id = props.id  with n, uuid
                       |Return properties(n) as nodeProps, uuid
                       |""".stripMargin
    val findEmployee = s"""OPTIONAL Match (n:$employeeLabel) with n, uuid
                       |Return collect(properties(n)) as nodePropList, uuid
                       |""".stripMargin
    val query1 =
      QueryWithPromise(SubstitutableQuery(findPerson, Map("id" -> "1".asInstanceOf[Object]).asJava), Promise())
    val query2 =
      QueryWithPromise(SubstitutableQuery(findPerson, Map("id" -> "2".asInstanceOf[Object]).asJava), Promise())
    val query3 =
      QueryWithPromise(SubstitutableQuery(findEmployee), Promise())
    val readOp = new ReadOperationsStream(neo4jConnection)
    readOp.enqueue(query1)
    readOp.enqueue(query2)
    readOp.enqueue(query3)
    val result1 = Await.result(query1.promise.future, Duration.Inf).get("nodeProps").asMap()
    val result2 = Await.result(query2.promise.future, Duration.Inf).get("nodeProps").asMap()
    val result3 = Await.result(query3.promise.future, Duration.Inf).get("nodePropList").asList()

    result1 shouldBe Map("name" -> "P1", "id" -> "1").asJava
    result2 shouldBe Map("name" -> "P2", "id" -> "2").asJava
    result3.get(0) shouldBe Map("name" -> "E1", "id" -> "1").asJava
    result3.get(1) shouldBe Map("name" -> "E2", "id" -> "2").asJava

  }

  test("should fail the promises on Error") {
    implicit val actorSystem = ActorSystem(Behaviors.empty, "ReadOperationTest")

    val errorProneQuery = "ErrorProne Query"
    val query1 =
      QueryWithPromise(
        SubstitutableQuery(errorProneQuery, Map("id" -> "1".asInstanceOf[Object]).asJava),
        Promise()
      )
    val query2 =
      QueryWithPromise(
        SubstitutableQuery(errorProneQuery, Map("id" -> "2".asInstanceOf[Object]).asJava),
        Promise()
      )

    val readOp = new ReadOperationsStream(neo4jConnection, mockConfig)
    readOp.enqueue(query1)
    readOp.enqueue(query2)

    the[ClientException] thrownBy Await.result(query1.promise.future, Duration.Inf)
    the[ClientException] thrownBy Await.result(query1.promise.future, Duration.Inf)
  }

  test("should throw Error when enqueue fails") {

    val errorProneQuery = "ErrorProne Query"
    val query1 =
      QueryWithPromise(
        SubstitutableQuery(errorProneQuery, Map("id" -> "1".asInstanceOf[Object]).asJava),
        Promise()
      )

    val readOp = new ReadOperationsStream(neo4jConnection, mockConfig)
    readOp.close()
    the[EnqueueFailedException] thrownBy readOp.enqueue(query1)
  }
}

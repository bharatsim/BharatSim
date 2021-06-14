package com.bharatsim.engine.graph.neo4j

import java.net.URI
import java.util

import com.bharatsim.engine.basicConversions.encoders.DefaultEncoders._
import com.bharatsim.engine.graph.ingestion.{GraphData, Relation}
import com.bharatsim.engine.graph.patternMatcher.MatchCondition._
import com.bharatsim.engine.testModels.{TestCitizen, TestHome}
import com.dimafeng.testcontainers.{ForAllTestContainer, Neo4jContainer}
import org.neo4j.driver.Values.parameters
import org.neo4j.driver.{AuthTokens, Driver, GraphDatabase, Record, Transaction}
import org.scalatest.BeforeAndAfterEach
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec

import scala.collection.mutable
import scala.collection.mutable.ListBuffer
import scala.jdk.CollectionConverters.{ListHasAsScala, MapHasAsScala}

class Neo4jProviderTest extends AnyWordSpec with BeforeAndAfterEach with Matchers with ForAllTestContainer {
  override val container = Neo4jContainer()

  var neo4jConnection: Driver = null;
  var graphProvider: Neo4jProvider = null;

  override def beforeEach(): Unit = {
    val uri = container.boltUrl
    neo4jConnection = GraphDatabase.driver(uri, AuthTokens.basic(container.username, container.password))
    val config = Neo4jConfig(URI.create(uri), container.username, container.password)
    graphProvider = new Neo4jProvider(config)
  }

  override def afterEach(): Unit = {
    graphProvider.deleteAll()
  }

  "createNode" should {
    "create node in db when label and properties are provided" in {

      graphProvider.createNode("Person", ("name", "Ramesh"), ("age", 23))
      graphProvider.createNode("Person", Map(("name", "Suresh"), ("age", 27)))

      val result: ListBuffer[mutable.Map[String, AnyRef]] = neo4jConnection
        .session()
        .readTransaction(x => {
          val rs = x.run("MATCH (p:Person) return properties(p) as person")
          val listBuffer: ListBuffer[mutable.Map[String, AnyRef]] = ListBuffer.empty
          rs.list().forEach(r => listBuffer.addOne(r.get("person").asMap().asScala))
          x.close()

          listBuffer
        })

      result.map(_("name")) should contain theSameElementsAs List("Ramesh", "Suresh")
      result.map(_("age")) should contain theSameElementsAs List(23, 27)
    }
  }

  "createRelation" should {
    "create relation between two nodes" in {

      val rameshId = graphProvider.createNode("Person", ("name", "Ramesh"), ("age", 23))
      val sureshId = graphProvider.createNode("Person", Map(("name", "Suresh"), ("age", 27)))

      val label = "FRIEND"
      graphProvider.createRelationship(rameshId, label, sureshId);

      val result = neo4jConnection
        .session()
        .readTransaction[Int]((tx: Transaction) => {
          val result = tx.run(
            s"""MATCH (n) where id(n) = $$nodeId with n
               |MATCH (n)-[:$label]->(o)
               |RETURN id(o) as nodeId, properties(o) as node, labels(o) as nodeLabels
               |""".stripMargin,
            parameters("nodeId", rameshId)
          )
          result.next().get("nodeId").asInt()
        })

      result shouldBe sureshId
    }
  }

  "fetchNode" should {
    val label = "Person"
    "fetch one node" in {
      createPerson()
      val person = graphProvider.fetchNode(label, Map(("name", "Ramesh"))).get;
      person.getParams("age") shouldBe 23
    }

    "fetch multiple node" in {
      createPerson()
      val result1 = graphProvider.fetchNodes(label, Map(("city", "pune")));
      val result2 = graphProvider.fetchNodes(label, ("city", "pune"));
      result1.map(_.getParams("name")).toList should contain theSameElementsAs List("Ramesh", "Suresh")
      result2.map(_.getParams("name")).toList should contain theSameElementsAs List("Ramesh", "Suresh")
    }
    "fetch multiple node with pattern match" in {
      createPerson()
      val result = graphProvider.fetchNodes(label, "city" equ "pune");
      result.map(_.getParams("name")).toList should contain theSameElementsAs List("Ramesh", "Suresh")
    }
    "fetch node count" in {
      createPerson()
      val count = graphProvider.fetchCount(label, "city" equ "pune");
      count shouldBe 2
    }
  }

  "fetchNeighbors" should {

    "fetch Neighbors" in {
      val (rameshId, sureshId) = createFriendship()
      val persons = graphProvider.fetchNeighborsOf(rameshId, "FRIEND").toList;
      persons.head.Id shouldBe sureshId
      persons.head.getParams("name") shouldBe "Suresh"

    }

    "count Neighbors with condition" in {
      val (rameshId, _) = createFriendship()
      val count = graphProvider.neighborCount(rameshId, "FRIEND", "name" equ "Suresh");
      count shouldBe 1
    }

    "count Neighbors with unmatched condition" in {
      val (rameshId, _) = createFriendship()
      val count = graphProvider.neighborCount(rameshId, "FRIEND", "name" equ "Stranger");
      count shouldBe 0
    }

    "count Neighbors without specifying condition" in {
      val (rameshId, _) = createFriendship()
      val count = graphProvider.neighborCount(rameshId, "FRIEND");
      count shouldBe 1
    }
  }

  "delete node" should {
    "delete node with Id" in {
      val (rameshId, _) = createPerson()

      graphProvider.deleteNode(rameshId)
      val result: List[Int] = neo4jConnection
        .session()
        .readTransaction(x => {
          val rs = x.run("MATCH (p:Person) return id(p) as nodeId")
          rs.list[Int]((r) => r.get("nodeId").asInt()).asScala.toList;
        })

      result should not contain rameshId

    }

    "delete node with matching props" in {
      val (rameshId, _) = createPerson()

      graphProvider.deleteNodes("Person", Map(("name", "Ramesh")))
      val result: List[Int] = neo4jConnection
        .session()
        .readTransaction(x => {
          val rs = x.run("MATCH (p:Person) return id(p) as nodeId")
          rs.list[Int]((r) => r.get("nodeId").asInt()).asScala.toList;
        })

      result should not contain rameshId
    }
  }

  "deleteRelation" should {
    "delete relation between nodes" in {
      val (rameshId, sureshId) = createFriendship()
      graphProvider.deleteRelationship(rameshId, "FRIEND", sureshId)

      val result: Int = neo4jConnection
        .session()
        .readTransaction((tx: Transaction) => {
          val rs = tx.run(s"""MATCH (n) where id(n) = $rameshId with n
             |MATCH (n)-[:FRIEND]->(o)
             |RETURN id(o) as nodeId
             |""".stripMargin)
          rs.list().size()
        })
      result shouldBe 0
    }
  }

  "deleteAll" should {
    "delete all the data " in {
      createPerson()
      graphProvider.deleteAll()

      val personCount: Int = neo4jConnection
        .session()
        .readTransaction(x => {
          val rs = x.run("MATCH (p:Person) return id(p) as nodeId")
          rs.list().size()
        })

      personCount shouldBe 0
    }
  }

  "updateNode" should {
    "update the node parameters" in {
      val (rameshId, _) = createPerson()
      graphProvider.updateNode(rameshId, Map(("age", 33)))
      graphProvider.updateNode(rameshId, ("city", "mumbai"), ("height", 5))

      val result: ListBuffer[mutable.Map[String, AnyRef]] = neo4jConnection
        .session()
        .readTransaction(tx => {
          val rs = tx.run(s"MATCH (p:Person) where id(p)=$rameshId return properties(p) as person")
          val listBuffer: ListBuffer[mutable.Map[String, AnyRef]] = ListBuffer.empty
          rs.list().forEach(r => listBuffer.addOne(r.get("person").asMap().asScala))
          tx.close()
          listBuffer
        })

      val rameshProps = result.head
      rameshProps("age") shouldBe 33
      rameshProps("city") shouldBe "mumbai"
      rameshProps("height") shouldBe 5
    }
  }

  "ingestFromCsv" should {

    "create nodes and relations from CSV file" in {
      val filePath = "src/test/scala/com/bharatsim/engine/graph/sample.csv"
      val mapper = Some((map: Map[String, String]) => {
        val nodeId = map("id").toInt
        val age = map("age").toInt

        val citizenNode = TestCitizen(age)
        val homeId = map("house_id").toInt
        val home = TestHome()
        val staysAt = Relation[TestCitizen, TestHome](nodeId, "STAYS_AT", homeId)
        val memberOf = Relation[TestHome, TestCitizen](homeId, "HOUSES", nodeId)
        val graphData = GraphData()
        graphData.addRelations(staysAt, memberOf)
        graphData.addNode(nodeId, citizenNode)
        graphData.addNode(homeId, home)
        graphData
      })

      graphProvider.ingestFromCsv(filePath, mapper)

      val citizens: mutable.Buffer[(Int, Int)] = neo4jConnection
        .session()
        .readTransaction[util.List[(Int, Int)]]((tx: Transaction) => {
          val result = tx.run(s"MATCH (n:TestCitizen) RETURN properties(n) AS node, id(n) AS nodeId")
          result.list[(Int, Int)]((record: Record) => {
            val age = record.get("node").get("age").asInt()
            val nodeId = record.get("nodeId").asInt()
            (nodeId, age)
          })
        })
        .asScala;

      val citizenIds = neo4jConnection
        .session()
        .readTransaction[util.List[Int]]((tx: Transaction) => {
          val houseId = tx.run(s"MATCH (n:TestHome) RETURN id(n) AS nodeId").next().get("nodeId").asInt()
          val result = tx.run(
            s"""MATCH (n) where id(n) = $$houseId with n
               |MATCH (n)-[:HOUSES]->(o)
               |RETURN id(o) as nodeId, properties(o) as node, labels(o) as nodeLabels
               |""".stripMargin,
            parameters("houseId", houseId)
          )
          result.list[Int](_.get("nodeId").asInt())
        });

      citizens.map(_._2) should contain theSameElementsAs List(25, 35)

      citizenIds should contain theSameElementsAs citizens.map(_._1)

    }
  }

  "shoutdown" should {
    "close the connection" in {
      val uri = container.boltUrl
      neo4jConnection = GraphDatabase.driver(uri, AuthTokens.basic(container.username, container.password))
      val config = Neo4jConfig(URI.create(uri), container.username, container.password)
      val tempGraphProvider = new Neo4jProvider(config)
      tempGraphProvider.shutdown()
      val exception = the[IllegalStateException] thrownBy tempGraphProvider.fetchCount("Person", "name" equ "Ramesh")
      exception.getMessage shouldBe "This driver instance has already been closed"
    }
  }

  private def createPerson(): (Int, Int) = {
    neo4jConnection.session.writeTransaction((tx: Transaction) => {
      val label = "Person"
      val rameshId = tx
        .run(
          s"CREATE (n:$label) SET n=$$props return id(n) as nodeId",
          parameters(
            "props",
            new util.HashMap[String, Any]() {
              put("name", "Ramesh")
              put("age", 23)
              put("city", "pune")
            }
          )
        )
        .next()
        .get("nodeId")
        .asInt()
      val sureshId = tx
        .run(
          s"CREATE (n:$label) SET n=$$props return id(n) as nodeId",
          parameters(
            "props",
            new util.HashMap[String, Any]() {
              put("name", "Suresh")
              put("age", 25)
              put("city", "pune")

            }
          )
        )
        .next()
        .get("nodeId")
        .asInt()

      (rameshId, sureshId)
    })
  }
  private def createFriendship(): (Int, Int) = {
    val (rameshId, sureshId) = createPerson()
    neo4jConnection.session.writeTransaction((tx: Transaction) => {
      tx.run(
        s"""
           |OPTIONAL MATCH (node1) WHERE id(node1) = $$nodeId1
           |OPTIONAL MATCH (node2) WHERE id(node2) = $$nodeId2
           |CREATE (node1)-[:FRIEND]-> (node2)
           |""".stripMargin,
        parameters("nodeId1", rameshId, "nodeId2", sureshId)
      )

    })
    (rameshId, sureshId)
  }
}

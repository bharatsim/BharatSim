package com.bharatsim.engine.graph.neo4j

import org.neo4j.driver.GraphDatabase
import org.neo4j.harness.Neo4jBuilders
import org.scalatest.BeforeAndAfterEach
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec

import scala.collection.mutable
import scala.collection.mutable.ListBuffer
import scala.jdk.CollectionConverters.MapHasAsScala

class Neo4jProviderTest extends AnyWordSpec with Matchers with BeforeAndAfterEach {

  "createNode" ignore {
    "create node in db when label and properties are provided" in {
      val neo4j = Neo4jBuilders.newInProcessBuilder()
        .withFixture("MATCH (n) detach delete n")
        .build()

      val uri = neo4j.boltURI()
      val neo4jConnection = GraphDatabase.driver(uri)

      val config = Neo4jConfig(uri)
      val graphProvider = new Neo4jProvider(config)

      graphProvider.createNode("Person", ("name", "Ramesh"), ("age", 23))
      graphProvider.createNode("Person", Map(("name", "Suresh"), ("age", 27)))

      val result: ListBuffer[mutable.Map[String, AnyRef]] = neo4jConnection
        .session()
        .readTransaction(x => {
          val rs = x.run("MATCH (p:Person) return properties(p) as person")
          val listBuffer: ListBuffer[mutable.Map[String, AnyRef]] = ListBuffer.empty
          rs.list().forEach(r => listBuffer.addOne(r.asMap().asScala))
          x.close()

          listBuffer
        })

      result.size shouldBe 2
    }
  }
}

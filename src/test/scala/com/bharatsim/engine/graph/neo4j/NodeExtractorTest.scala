package com.bharatsim.engine.graph.neo4j

import com.bharatsim.engine.graph.ingestion.{CsvNode, GraphData, Relation}
import com.bharatsim.engine.graph.neo4j.NodeExtractorTest.{data, extractor}
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec

import scala.collection.mutable.ListBuffer

class NodeExtractorTest extends AnyWordSpec with Matchers {
  "fetchNodes" should {
    "return nodes aggregated per label" in {
      val nodeExtractor = new NodeExtractor(data, extractor)

      val nodes = nodeExtractor.fetchNodes.toList
      nodes.size shouldBe 2
      nodes.head._1 shouldBe "Office"
      val offices = nodes.head._2.toList
      offices.size shouldBe 1
      offices.head.params.apply("id") shouldBe 123

      nodes.last._1 shouldBe "Person"
      val people = nodes.last._2.toList
      people.size shouldBe 2
      people.head.params.apply("name") shouldBe "Raghav"
      people.last.params.apply("age") shouldBe "54"
    }
  }

  "fetchRelations" should {
    "return relationships" in {
      val nodeExtractor = new NodeExtractor(data, extractor)

      val relations = nodeExtractor.fetchRelations.toList
      relations.size shouldBe 1
      relations should contain theSameElementsAs List(
        ("WORKS_AT", ListBuffer(Relation(1, "WORKS_AT", 123), Relation(2, "WORKS_AT", 123)))
      )
    }
  }
}

private[neo4j] object NodeExtractorTest {
  val data: Seq[Map[String, String]] = List(
    Map("id" -> "1", "name" -> "Raghav", "age" -> "34", "height" -> "155", "officeId" -> "123"),
    Map("id" -> "2", "name" -> "Ramesh", "age" -> "54", "height" -> "157", "officeId" -> "123")
  )

  def extractor(row: Map[String, String]): GraphData = {
    val data = new GraphData()
    val citizenId = row("id").toInt
    val officeId = row("officeId").toInt

    data._nodes.addOne(CsvNode("Person", citizenId, row.removedAll(List("id", "officeId"))))
    data._nodes.addOne(CsvNode("Office", officeId, Map("id" -> officeId)))

    data.addRelations(Relation(citizenId, "WORKS_AT", officeId))
    data
  }
}

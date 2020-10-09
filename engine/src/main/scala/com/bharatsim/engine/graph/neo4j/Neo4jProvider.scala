package com.bharatsim.engine.graph.neo4j

import java.util

import com.bharatsim.engine.graph.GraphProvider.NodeId
import com.bharatsim.engine.graph._
import com.github.tototoshi.csv.CSVReader
import com.typesafe.scalalogging.LazyLogging
import org.neo4j.driver.Values.{parameters, value}
import org.neo4j.driver.{AuthTokens, GraphDatabase, Record, Transaction}

import scala.collection.mutable
import scala.collection.mutable.ListBuffer
import scala.jdk.CollectionConverters.{ListHasAsScala, MapHasAsJava, MapHasAsScala, SeqHasAsJava}

class Neo4jProvider(config: Neo4jConfig) extends GraphProvider with LazyLogging {
  private val neo4jConnection = config.username match {
    case Some(_) => GraphDatabase.driver(config.uri, AuthTokens.basic(config.username.get, config.password.get))
    case None => GraphDatabase.driver(config.uri)
  }

  private[engine] override def createNode(label: String, props: (String, Any)*): NodeId = createNode(label, props.toMap)

  private[engine] override def createNode(label: String, props: Map[String, Any]): NodeId = {
    val session = neo4jConnection.session()

    val nodeId = session.writeTransaction((tx: Transaction) => {
      val javaMap = new util.HashMap[String, java.lang.Object]()
      props.foreach(kv => javaMap.put(kv._1, kv._2.asInstanceOf[java.lang.Object]))

      val result = tx.run(s"CREATE (n:$label) SET n=$$props return id(n) as nodeId", parameters("props", javaMap))

      result.next().get("nodeId").asInt()
    })

    session.close()
    nodeId
  }

  override def createRelationship(node1: NodeId, label: String, node2: NodeId): Unit = {
    val session = neo4jConnection.session()

    try {
      session.writeTransaction((tx: Transaction) => {
        tx.run(
          s"""
             |OPTIONAL MATCH (node1) WHERE id(node1) = $$nodeId1
             |OPTIONAL MATCH (node2) WHERE id(node2) = $$nodeId2
             |CREATE (node1)-[:$label]-> (node2)
             |""".stripMargin,
          parameters("nodeId1", node1, "nodeId2", node2)
        )
      })
    } catch {
      case e: Exception => logger.error(s"Failed to create relation '{}' due to reason -> {}", label, e.getMessage)
    } finally {
      session.close()
    }
  }

  override def ingestFromCsv(csvPath: String, mapper: Option[Function[Map[String, String], GraphData]]): Unit = {
    val reader = CSVReader.open(csvPath)
    val records = reader.allWithHeaders()

    val nodeTypes = mutable.HashMap[String, mutable.ListBuffer[CsvNode]]().empty
    val relations = ListBuffer[Relation]().empty
    val refToIdMapping = mutable.HashMap[Int, NodeId]().empty

    if (mapper.isDefined) {
      val extractor = mapper.get
      records.foreach(m => {
        val graphData = extractor(m)
        relations.addAll(graphData.relations)
        graphData.nodes.foreach(node => {
          if (!nodeTypes.contains(node.label)) {
            val list = ListBuffer[CsvNode]().empty
            nodeTypes(node.label) = list
          }
          nodeTypes(node.label).addOne(node)
        })
      })
    }

    nodeTypes.foreach(kv => {
      val label = kv._1
      val nodes = kv._2

      nodes.foreach(node => {
        if (!refToIdMapping.contains(node.uniqueRef)) {
          val nodeId = createNode(label, node.params)
          refToIdMapping(node.uniqueRef) = nodeId
        }
      })
    })

    relations.foreach(rel => {
      val fromId = refToIdMapping(rel.refFrom)
      val toId = refToIdMapping(rel.refTo)
      createRelationship(fromId, rel.relation, toId)
    })
  }

  override def fetchNode(label: String, params: Map[String, Any]): Option[GraphNode] = {
    val session = neo4jConnection.session()

    val retValue = session.readTransaction((tx: Transaction) => {
      val paramsMapJava = params.map(kv => (kv._1, value(kv._2))).asJava

      val result = tx.run(makeMatchNodeQuery(label, params, Some(1)), value(paramsMapJava))

      if (result.hasNext) {
        val record = result.next()
        Some(extractGraphNode(label, record))
      } else {
        None
      }
    })

    session.close()
    retValue
  }

  override def fetchNodes(label: String, params: Map[String, Any]): Iterable[GraphNode] = {
    val session = neo4jConnection.session()

    val nodes: util.List[GraphNode] = session.readTransaction((tx: Transaction) => {
      val paramsMapJava = params.map(kv => (kv._1, value(kv._2))).asJava

      val result = tx.run(makeMatchNodeQuery(label, params), value(paramsMapJava))

      result.list[GraphNode](record => extractGraphNode(label, record))
    })
    session.close()
    nodes.asScala
  }

  override def fetchNodes(label: String, params: (String, Any)*): Iterable[GraphNode] = fetchNodes(label, params.toMap)

  override def fetchNeighborsOf(nodeId: NodeId, label: String, labels: String*): Iterable[GraphNode] = {
    val session = neo4jConnection.session()
    val allLabels = label :: labels.toList
    val labelAOrLabelB = allLabels.mkString(" | ")

    val retValue = session
      .readTransaction((tx: Transaction) => {
        val result = tx.run(
          s"""MATCH (n) where id(n) = $$nodeId with n
             |MATCH (n)-[:$labelAOrLabelB]->(o)
             |RETURN id(o) as nodeId, properties(o) as node, labels(o) as nodeLabels
             |""".stripMargin,
          parameters("nodeId", nodeId)
        )

        result.list(record => extractGraphNode("", record))
      })

    session.close()
    retValue.asScala
  }

  override def updateNode(nodeId: NodeId, props: Map[String, Any]): Unit = {
    val session = neo4jConnection.session()
    val expandedProps = toMatchCriteria("n", props, ",")
    val query =
      s"""MATCH (n) where id(n) = $$nodeId
         |SET $expandedProps
         |""".stripMargin
    val params = props.+(("nodeId", nodeId)).map(kv => (kv._1, value(kv._2))).asJava

    session.writeTransaction(tx => {
      tx.run(query, value(params))
      tx.commit()
    })

    session.close()
  }

  override def updateNode(nodeId: NodeId, prop: (String, Any), props: (String, Any)*): Unit =
    updateNode(nodeId, (prop :: props.toList).toMap)

  override def deleteNode(nodeId: NodeId): Unit = {
    val query =
      """MATCH (n) where id(n) = $nodeId
        |DETACH DELETE n""".stripMargin

    val session = neo4jConnection.session()

    session.writeTransaction(tx => {
      tx.run(query, parameters("nodeId", nodeId))
      tx.commit()
    })

    session.close()
  }

  override def deleteRelationship(from: NodeId, label: String, to: NodeId): Unit = {
    val query =
      s"""MATCH (a) where id(a) = $$from
         |MATCH (b) where id(b) = $$to
         |MATCH (a)-[rel:$label]->(b)
         |DELETE rel""".stripMargin

    val session = neo4jConnection.session()

    session.writeTransaction(tx => {
      tx.run(query, parameters("from", from, "to", to))
      tx.commit()
    })

    session.close()
  }

  override def deleteNodes(label: String, props: Map[String, Any]): Unit = {
    val matchCriteria = toMatchCriteria("n", props, "AND")
    val query =
      s"""MATCH (n:$label) WHERE $matchCriteria
         |DETACH DELETE n""".stripMargin

    val session = neo4jConnection.session()

    session.writeTransaction(tx => {
      tx.run(query, value(props.map(kv => (kv._1, value(kv._2))).asJava))
      tx.commit()
    })
    session.close()
  }

  override def deleteAll(): Unit = {
    val session = neo4jConnection.session()
    val query = "MATCH (n) detach delete n"

    session.writeTransaction((tx: Transaction) => {
      tx.run(query)
      tx.commit()
    })

    session.close()
  }

  private def toMatchCriteria(variableName: String, params: Map[String, Any], separator: String) = {
    params.keys.map(key => s"$variableName.$key = $$$key").mkString(s" $separator ")
  }

  private def makeMatchNodeQuery(label: String, params: Map[String, Any], limit: Option[Int] = None) = {
    val limitClause = if (limit.isDefined) s"LIMIT ${limit.get}" else ""

    if (params.isEmpty) s"MATCH (n:$label) RETURN properties(n) AS node, id(n) AS nodeId $limitClause"
    else {
      val matchCriteria = toMatchCriteria("n", params, "and")
      s"MATCH (n:$label) WHERE $matchCriteria RETURN properties(n) AS node, id(n) AS nodeId $limitClause"
    }
  }

  private def extractGraphNode(label: String, record: Record) = {
    val node = record.get("node").asMap()
    val nodeId = record.get("nodeId").asInt()
    val extractedLabel = record.get("nodeLabels").asList(List[AnyRef](label).asJava)

    val mapWithValueTypeAny = node.asScala.map(kv => (kv._1, kv._2.asInstanceOf[Any])).toMap
    new GraphNodeImpl(extractedLabel.get(0).toString, nodeId, mapWithValueTypeAny)
  }

  override def shutdown(): Unit = neo4jConnection.close()
}

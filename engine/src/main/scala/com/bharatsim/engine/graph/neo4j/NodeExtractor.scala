package com.bharatsim.engine.graph.neo4j

import com.bharatsim.engine.graph.{CsvNode, GraphData, Relation}

import scala.collection.mutable
import scala.collection.mutable.ListBuffer

class NodeExtractor(records: Seq[Map[String, String]], mapper: Function[Map[String, String], GraphData]) {
  private val relations = ListBuffer[Relation]().empty
  private val nodeTypes = mutable.HashMap[String, mutable.HashMap[Int, CsvNode]]().empty

  def fetchNodes: Iterator[(String, Iterable[CsvNode])] = nodeTypes.map(kv => (kv._1, kv._2.values)).iterator

  process()

  def fetchRelations: Iterator[(String, ListBuffer[Relation])] = relations.foldLeft(new mutable.HashMap[String, ListBuffer[Relation]]())((acc, rel) => {
    if (!acc.contains(rel.relation)) acc.put(rel.relation, ListBuffer[Relation]().empty)
    acc(rel.relation).addOne(rel)
    acc
  }).iterator

  private def process(): Unit = {
    records.foreach(row => {
      val graphData = mapper(row)
      relations.addAll(graphData.relations)
      graphData.nodes.foreach(node => {
        if (!nodeTypes.contains(node.label)) {
          val list = mutable.HashMap[Int, CsvNode]().empty
          nodeTypes(node.label) = list
        }
        nodeTypes(node.label).put(node.uniqueRef, node)
      })
    })
  }

}
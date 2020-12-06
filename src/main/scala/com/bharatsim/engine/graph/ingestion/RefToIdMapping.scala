package com.bharatsim.engine.graph.ingestion

import scala.collection.mutable

private[engine] class RefToIdMapping {
  private val mapping = mutable.HashMap.empty[String, mutable.HashMap[Int, Int]]

  def addMappings(nodeLabel: String, mappings: IterableOnce[(Int, Int)]): Unit = {
    mappings.iterator.foreach(m => addMapping(nodeLabel, m._1, m._2))
  }

  def addMapping(nodeLabel: String, uniqueRef: Int, internalId: Int): Unit = {
    if (mapping.contains(nodeLabel)) {
      mapping(nodeLabel).put(uniqueRef, internalId)
    } else {
      val newMapping = mutable.HashMap.empty[Int, Int]
      newMapping.put(uniqueRef, internalId)
      mapping.put(nodeLabel, newMapping)
    }
  }

  def hasReference(ref: Int, label: String): Boolean = {
    mapping.get(label) match {
      case Some(map) => map.contains(ref)
      case None => false
    }
  }

  def getFor(label: String, ref: Int): Option[Int] = {
    mapping.get(label) match {
      case Some(map) => map.get(ref)
      case None => None
    }
  }
}

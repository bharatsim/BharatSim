package com.bharatsim.engine.graph.ingestion

import scala.collection.mutable

private[engine] class RefToIdMapping {
  private val mapping = mutable.HashMap.empty[String, mutable.HashMap[Long, Long]]

  def addMappings(nodeLabel: String, mappings: IterableOnce[(Long, Long)]): Unit = {
    mappings.iterator.foreach(m => addMapping(nodeLabel, m._1, m._2))
  }

  def addMapping(nodeLabel: String, uniqueRef: Long, internalId: Long): Unit = {
    if (mapping.contains(nodeLabel)) {
      mapping(nodeLabel).put(uniqueRef, internalId)
    } else {
      val newMapping = mutable.HashMap.empty[Long, Long]
      newMapping.put(uniqueRef, internalId)
      mapping.put(nodeLabel, newMapping)
    }
  }

  def hasReference(ref: Long, label: String): Boolean = {
    mapping.get(label) match {
      case Some(map) => map.contains(ref)
      case None => false
    }
  }

  def getFor(label: String, ref: Long): Option[Long] = {
    mapping.get(label) match {
      case Some(map) => map.get(ref)
      case None => None
    }
  }
}

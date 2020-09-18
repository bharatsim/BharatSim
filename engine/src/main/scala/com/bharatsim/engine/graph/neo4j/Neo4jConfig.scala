package com.bharatsim.engine.graph.neo4j

import java.net.URI

case class Neo4jConfig(uri: URI, username: Option[String], password: Option[String])

object Neo4jConfig {
  def apply(uri: URI): Neo4jConfig = new Neo4jConfig(uri, None, None)

  def apply(uri: URI, username: String, password: String): Neo4jConfig = new Neo4jConfig(uri, Some(username), Some(password))
}

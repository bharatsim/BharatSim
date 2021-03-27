package com.bharatsim.engine.distributed

import org.neo4j.driver.Bookmark
import java.util

case class DBBookmark(values: util.Set[String]) extends CborSerializable

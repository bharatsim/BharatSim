package com.bharatsim.engine.distributed

import java.util

case class DBBookmark(values: util.Set[String]) extends CborSerializable

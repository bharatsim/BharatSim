package com.bharatsim.engine.graph.neo4j.queryBatching

import java.util

import scala.collection.mutable.ListBuffer

sealed trait SubstitutableString {
  override def equals(obj: Any): Boolean = {
    (this, obj) match {
      case (SingleParamString(f1), SingleParamString(f2)) => f1("a") == f2("b")
      case (TwoParamString(f1), TwoParamString(f2))       => f1("a", "b") == f2("a", "b")
      case (ThreeParamString(f1), ThreeParamString(f2))   => f1("a", "b", "c") == f2("a", "b", "c")
      case (FourParamString(f1), FourParamString(f2))     => f1("a", "b", "c", "d") == f2("a", "b", "c", "d")
      case (FiveParamString(f1), FiveParamString(f2))     => f1("a", "b", "c", "d", "e") == f2("a", "b", "c", "d", "e")
      case _                                              => super.equals(obj)
    }
  }
}

case class SingleParamString(get: String => String) extends SubstitutableString
case class TwoParamString(get: (String, String) => String) extends SubstitutableString
case class ThreeParamString(get: (String, String, String) => String) extends SubstitutableString
case class FourParamString(get: (String, String, String, String) => String) extends SubstitutableString
case class FiveParamString(get: (String, String, String, String, String) => String) extends SubstitutableString

case class SubstituableQuery(queryBody: SubstitutableString, props: util.Map[String, java.lang.Object] = new util.HashMap[String, java.lang.Object]())

case class GroupQuery(query: SubstitutableString, multiProps: ListBuffer[util.Map[String, java.lang.Object]])

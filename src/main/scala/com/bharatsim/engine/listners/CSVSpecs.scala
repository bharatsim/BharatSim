package com.bharatsim.engine.listners

trait CSVSpecs {
  def getHeaders(): List[String]
  def getValue(fieldName: String): Any
}

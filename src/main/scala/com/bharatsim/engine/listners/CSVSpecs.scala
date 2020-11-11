package com.bharatsim.engine.listners

/**
  * CSVSpecs has to be implemented to provide data of CSV to [[CsvOutputGenerator]]
  */
trait CSVSpecs {

  /**
    * @return A list of Headers for CSV
    */
  def getHeaders(): List[String]

  /**
    * Gets the value for header
    * @param fieldName is one of the header from `getHeaders`
    * @return is value associated with header
    */
  def getValue(fieldName: String): Any
}

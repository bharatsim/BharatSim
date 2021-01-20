package com.bharatsim.engine.listeners

/**
  * CSVSpecs has to be implemented to provide data of CSV to [[CsvOutputGenerator]]
  */
trait CSVSpecs {

  /**
    * @return A list of Headers for CSV
    */
  def getHeaders: List[String]

  /**
    * Gets list of rows value. each row has value in same order as headers.
    *
    * @return list of rows
    */
  def getRows(): List[List[Any]]
}

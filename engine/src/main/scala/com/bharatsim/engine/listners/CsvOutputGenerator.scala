package com.bharatsim.engine.listners

import com.bharatsim.engine.Context
import com.github.tototoshi.csv.CSVWriter

import scala.collection.mutable.ListBuffer

class CsvOutputGenerator(path: String, csvSpecs: CSVSpecs, val openCsvWriter: (String) => CSVWriter = CSVWriter.open)
    extends SimulationListener {

  private val rows = new ListBuffer[List[Any]]

  override def onSimulationStart(context: Context): Unit = {}

  override def onStepStart(context: Context): Unit = {
    val row = csvSpecs.getHeaders().map(csvSpecs.getValue)
    rows.addOne(row);
  }
  override def onStepEnd(context: Context): Unit = {}

  override def onSimulationEnd(context: Context): Unit = {
    val writer = openCsvWriter(path)
    writer.writeRow(csvSpecs.getHeaders())
    writer.writeAll(rows.toList)
    writer.close()
  }

}

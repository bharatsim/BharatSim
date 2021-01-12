package com.bharatsim.engine.listeners

import com.bharatsim.engine.Context
import com.github.tototoshi.csv.CSVWriter

/**
  * CsvOutputGenerator is [[com.bharatsim.engine.listeners.SimulationListener SimulationListener]].
  * it collects the value for CSV at the start of every step and writes it to CSV at the end of simulation.
  *
  * @param path is the path where CSV is to be created.
  * @param csvSpecs is specification of csv headers and values.
  * @param openCsvWriter [Optional] is function that allows to customise CSVWriter.
  */
class CsvOutputGenerator(
                          path: String,
                          csvSpecs: CSVSpecs,
                          private val openCsvWriter: String => CSVWriter = CSVWriter.open
) extends SimulationListener {
  private val writer = openCsvWriter(path)

  override def onSimulationStart(context: Context): Unit = {
    writer.writeRow(csvSpecs.getHeaders)
  }

  override def onStepStart(context: Context): Unit = {
    val row = csvSpecs.getHeaders.map(csvSpecs.getValue)
    writer.writeRow(row)
  }

  override def onStepEnd(context: Context): Unit = {}

  override def onSimulationEnd(context: Context): Unit = {
    writer.close()
  }

}

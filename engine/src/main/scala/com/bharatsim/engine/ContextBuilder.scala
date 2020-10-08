package com.bharatsim.engine

import com.bharatsim.engine.graph.GraphData

object ContextBuilder {
  def registerSchedules(s1: (Schedule, (Agent, Context) => Boolean), more: (Schedule, (Agent, Context) => Boolean)*)(implicit context: Context): Unit = {
    val allSchedules = s1 :: more.toList
    context.registerSchedules(allSchedules)
  }

  def ingestCSVData(path: String, fn: Map[String, String] => GraphData)(implicit context: Context): Unit = {
    context.graphProvider.ingestFromCsv(path, Some(fn))
  }

  def ingestDataUsingCSV(path: String)(implicit context: Context): Unit = {
    context.graphProvider.ingestFromCsv(path, None)
  }
}

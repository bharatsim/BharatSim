package com.bharatsim.model

import com.github.tototoshi.csv.{CSVReader, CSVWriter}

import scala.reflect.io.File

object Aggregator {
  private val TICKS_TO_AGGREGATE = 24
  private val inputFilePath = "src/main/resources/output-5M-buffering.csv"

  def main(): Unit = {
    val reader = CSVReader.open(inputFilePath)
    val splitPath: Array[String] = inputFilePath.split(File.separator.charAt(0))
    val outputFileName = s"aggregated-${splitPath.last}"
    val outputFilePath = (splitPath.take(splitPath.length - 1) :+ outputFileName).mkString(File.separator)
    val writer = CSVWriter.open(outputFilePath)

    val value = reader.all()
    value
      .takeRight(value.size - 1)
      .grouped(TICKS_TO_AGGREGATE)
      .foreach(rows => {
        val aggregated = rows.foldLeft(Array(0, 0, 0, 0, 0, 0, 0, 0, 0))((acc, row) => {
          val step = row.head.toInt
          if(step % 24 == 1) {
            acc(0) = step
            for((a, i) <- row.zipWithIndex) {
              if(i != 0) acc(i) = a.toInt
            }
          }
          acc
        })

        writer.writeRow(aggregated)
      })
  }
}

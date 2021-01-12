package com.bharatsim.engine.listeners

import com.bharatsim.engine.Context
import com.github.tototoshi.csv.CSVWriter
import org.mockito.MockitoSugar.{mock, spyLambda, verify, when}
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec

class CsvOutputGeneratorTest extends AnyWordSpec with Matchers {
  "it" should {
    "open csv writer on simulation start" in {
      val mockSpecs = mock[CSVSpecs]
      val csvWriterMock = mock[CSVWriter]
      val openCsvMock = spyLambda((filePath: String) => csvWriterMock)

      new CsvOutputGenerator("path", mockSpecs, openCsvMock)

      verify(openCsvMock).apply("path")
    }
  }

  "onSimulationStart" should {
    "write csv headers" in {
      val mockSpecs = mock[CSVSpecs]
      val testHeaders = List("header1", "header2")
      when(mockSpecs.getHeaders).thenReturn(testHeaders)
      val csvWriterMock = mock[CSVWriter]
      val openCsvMock = spyLambda((filePath: String) => csvWriterMock)
      val mockContext = mock[Context]

      new CsvOutputGenerator("path", mockSpecs, openCsvMock).onSimulationStart(mockContext)

      verify(csvWriterMock).writeRow(testHeaders)
    }
  }

  "onStepStart" should {
    "write values to csv" in {
      val mockSpecs = mock[CSVSpecs]
      val testHeaders = List("header1", "header2")
      when(mockSpecs.getHeaders).thenReturn(testHeaders)
      when(mockSpecs.getValue("header1")).thenReturn("value1")
      when(mockSpecs.getValue("header2")).thenReturn("value2")
      val csvWriterMock = mock[CSVWriter]
      val openCsvMock = spyLambda((filePath: String) => csvWriterMock)
      val mockContext = mock[Context]

      new CsvOutputGenerator("path", mockSpecs, openCsvMock).onStepStart(mockContext)

      verify(csvWriterMock).writeRow(List("value1", "value2"))
    }
  }

  "onSimulationEnd" should {
    "close the csvWriter" in {
      val mockSpecs = mock[CSVSpecs]
      val csvWriterMock = mock[CSVWriter]
      val openCsvMock = spyLambda((filePath: String) => csvWriterMock)
      val mockContext = mock[Context]

      new CsvOutputGenerator("path", mockSpecs, openCsvMock).onSimulationEnd(mockContext)

      verify(csvWriterMock).close()
    }
  }
}

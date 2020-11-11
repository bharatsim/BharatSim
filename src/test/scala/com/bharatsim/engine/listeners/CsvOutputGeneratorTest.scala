package com.bharatsim.engine.listeners

import java.io.FileOutputStream

import com.bharatsim.engine.Context
import com.github.tototoshi.csv.CSVWriter
import org.mockito.MockitoSugar
import org.scalatest.funsuite.AnyFunSuite
import org.scalatest.matchers.should.Matchers

class CsvOutputGeneratorTest extends AnyFunSuite with Matchers with MockitoSugar {

  test("should write to csv on simulation end") {
    val path = "output.csv"
    val csvSpecsMock = mock[CSVSpecs]
    val header = "foo"
    val rowValue = "bar"

    when(csvSpecsMock.getHeaders()).thenReturn(List(header))
    when(csvSpecsMock.getValue(header)).thenReturn(rowValue)

    val contextMock = mock[Context]
    val csvWriterMock = mock[CSVWriter]
    val openCsvMock = spyLambda((filePath: String) => csvWriterMock)

    val csvOutputGenerator = new CsvOutputGenerator(path, csvSpecsMock, openCsvMock)

    csvOutputGenerator.onStepStart(contextMock)
    verify(csvSpecsMock).getHeaders()
    verify(csvSpecsMock).getValue(header)

    csvOutputGenerator.onSimulationEnd(contextMock)

    verify(openCsvMock)(path)
    verify(csvWriterMock).writeRow(List(header))
    verify(csvWriterMock).writeAll(List(List(rowValue)))
    verify(csvWriterMock).close()
  }

}

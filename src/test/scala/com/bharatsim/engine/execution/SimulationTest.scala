package com.bharatsim.engine.execution

import com.bharatsim.engine.Context
import com.bharatsim.engine.execution.executors.{ExecutorFactory, SimulationExecutor}
import org.mockito.MockitoSugar
import org.scalatest.funsuite.AnyFunSuite

class SimulationTest extends AnyFunSuite with MockitoSugar {

  test("should execute the simulation") {
    val mockIngestion = spyLambda((context: Context) => {})
    val mockBody = spyLambda((context: Context) => {})
    val mockOnComplete = spyLambda((context: Context) => {})

    val mockExecutor = mock[SimulationExecutor]
    val mockExecutorFactory = mock[ExecutorFactory]
    when(mockExecutorFactory.getExecutor()).thenReturn(mockExecutor)
    val simulation = new Simulation(mockExecutorFactory)
    simulation.ingestData(mockIngestion)
    simulation.defineSimulation(mockBody)
    simulation.onCompleteSimulation(mockOnComplete)

    simulation.run()
    verify(mockExecutor).execute(SimulationDefinition(mockIngestion, mockBody, mockOnComplete))

  }
}

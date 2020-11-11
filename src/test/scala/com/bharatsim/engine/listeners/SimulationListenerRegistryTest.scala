package com.bharatsim.engine.listeners

import com.bharatsim.engine.Context
import org.mockito.MockitoSugar
import org.scalatest.funsuite.AnyFunSuite
import org.scalatest.matchers.should.Matchers

class SimulationListenerRegistryTest extends AnyFunSuite with Matchers with MockitoSugar {

  test("should notify to all registered listeners") {

    val mockContext = mock[Context];

    val listener1 = mock[SimulationListener]
    val listener2 = mock[SimulationListener]
    SimulationListenerRegistry.register(listener1)
    SimulationListenerRegistry.register(listener2)

    SimulationListenerRegistry.notifySimulationStart(mockContext)
    verify(listener1).onSimulationStart(mockContext)
    verify(listener2).onSimulationStart(mockContext)

    SimulationListenerRegistry.notifySimulationEnd(mockContext)
    verify(listener1).onSimulationEnd(mockContext)
    verify(listener2).onSimulationEnd(mockContext)

    SimulationListenerRegistry.notifyStepStart(mockContext)
    verify(listener1).onStepStart(mockContext)
    verify(listener2).onStepStart(mockContext)

    SimulationListenerRegistry.notifyStepEnd(mockContext)
    verify(listener1).onStepEnd(mockContext)
    verify(listener2).onSimulationEnd(mockContext)
  }
}

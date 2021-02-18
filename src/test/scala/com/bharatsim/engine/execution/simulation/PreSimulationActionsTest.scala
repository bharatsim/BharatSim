package com.bharatsim.engine.execution.simulation

import com.bharatsim.engine.Context
import com.bharatsim.engine.listeners.{SimulationListener, SimulationListenerRegistry}
import org.mockito.MockitoSugar.{mock, verify, when}
import org.scalatest.wordspec.AnyWordSpec

class PreSimulationActionsTest extends AnyWordSpec {
  "execute" should {
    "notify simulation start" in {
      val mockListener = mock[SimulationListener]
      SimulationListenerRegistry.register(mockListener)
      val context: Context = mock[Context]
      when(context.agentLabels).thenReturn(List.empty)

      new PreSimulationActions(context).execute()

      verify(mockListener).onSimulationStart(context)
    }
  }
}

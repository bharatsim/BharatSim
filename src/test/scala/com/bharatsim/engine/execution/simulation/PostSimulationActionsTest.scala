package com.bharatsim.engine.execution.simulation

import com.bharatsim.engine.Context
import com.bharatsim.engine.execution.actions.PostSimulationActions
import com.bharatsim.engine.listeners.{SimulationListener, SimulationListenerRegistry}
import org.mockito.MockitoSugar.{mock, verify}
import org.scalatest.wordspec.AnyWordSpec

class PostSimulationActionsTest extends AnyWordSpec {
  "notify simulation end" in {
    val mockListener = mock[SimulationListener]
    SimulationListenerRegistry.register(mockListener)
    implicit val context: Context = mock[Context]

    new PostSimulationActions(context).execute()

    verify(mockListener).onSimulationEnd(context)
  }
}

package com.bharatsim.engine.execution

import com.bharatsim.engine.Context

case class SimulationDefinition(
    ingestionStep: Context => Unit,
    simulationBody: Context => Unit,
    onComplete: Context => Unit
)

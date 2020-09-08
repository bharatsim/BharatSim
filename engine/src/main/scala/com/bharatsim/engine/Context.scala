package com.bharatsim.engine

import com.bharatsim.engine.graph.GraphProvider

class Context(val graphProvider: GraphProvider, val dynamics: Dynamics, val simulationContext: SimulationContext)

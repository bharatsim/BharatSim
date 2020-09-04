package com.bharatsim.engine

class Context() {
  val agents = new Collection[Agent]
  var simulationContext: SimulationContext = new SimulationContext
  var dynamics: Dynamics = new Dynamics

  def setDynamics(dynamic: Dynamics) {
    dynamics = dynamic
  }
}

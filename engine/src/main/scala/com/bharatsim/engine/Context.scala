package com.bharatsim.engine

class Context() {
  val agents = new Collection[Agent]
  var simulationContext: SimulationContext = new SimulationContext
  var dynamics: Dynamics = new Dynamics
  val numberOfHoursInADay: Int = 24
  val numberOfCitizen = 1000

  def setDynamics(dynamic: Dynamics) {
    dynamics = dynamic
  }
}

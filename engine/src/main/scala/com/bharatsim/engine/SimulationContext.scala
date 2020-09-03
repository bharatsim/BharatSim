package com.bharatsim.engine

class SimulationContext() {
  private[engine] var simulationSteps: Int = 1
  private var currentStep = 0

  def setSteps(steps: Int): Unit = {
    simulationSteps = steps
  }

  private[engine] def setCurrentStep(step: Int): Unit = {
    currentStep = step
  }

  def getCurrentStep: Int = currentStep
}

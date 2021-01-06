package com.bharatsim.model

import com.bharatsim.engine.Dynamics

object Disease extends Dynamics {
  final val asymptomaticPopulationPercentage = 0.3
  final val severeInfectedPopulationPercentage = 0.3

  final val preSymptomaticDuration = 4
  final val infectionRate: Double = 0.08
  final val exposedDuration: Int = 2
  final val lastDay: Int = 17
  final val deathRate = 0.02
}

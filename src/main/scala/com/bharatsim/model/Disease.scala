package com.bharatsim.model

import com.bharatsim.engine.Dynamics
import com.bharatsim.engine.distributions.LogNormal

object Disease extends Dynamics {
  final val asymptomaticPopulationPercentage = 0.3
  final val severeInfectedPopulationPercentage = 0.3

  final val exposedDurationProbabilityDistribution = LogNormal(4.6, 4.8)
  final val presymptomaticDurationProbabilityDistribution = LogNormal(1, 1)
  final val asymptomaticDurationProbabilityDistribution = LogNormal(8, 2)
  final val mildSymptomaticDurationProbabilityDistribution = LogNormal(8, 2)
  final val severeSymptomaticDurationProbabilityDistribution = LogNormal(14, 2.4)

  final val infectionRate: Double = 0.5
  final val deathRate = 0.02
  final val dt = 1.toDouble / 24.toDouble
  final val inverse_dt = 24.toDouble / 1.toDouble
}

package com.bharatsim.model

import com.bharatsim.engine.Dynamics

object Disease extends Dynamics {
  final val infectionRate: Double = 0.5

  final val asymptomaticPercentage: Double = 0.3
  final val mildSymptomaticPercentage: Double = 0.3

  final val transmissionStartDay: Int = 2
  final val exposedDuration: Int = 2
  final val preSymptomaticDuration: Int = 2
  final val lastDay: Int = 17
  final val asymptomaticLastDay: Int = 9
  final val mildSymptomaticLastDay: Int = 12

  final val deathRate = 0.02
}

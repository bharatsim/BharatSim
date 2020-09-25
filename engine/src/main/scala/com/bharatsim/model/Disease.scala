package com.bharatsim.model

import com.bharatsim.engine.Dynamics

object Disease extends Dynamics {
  final val infectionRate: Double = 0.5
  final val exposedDuration: Int = 2
  final val lastDay: Int = 17
  final val deathRate = 0.02
}

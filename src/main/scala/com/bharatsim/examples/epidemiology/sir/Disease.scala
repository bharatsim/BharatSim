package com.bharatsim.examples.epidemiology.sir

import com.bharatsim.engine.Dynamics
import com.bharatsim.engine.distributions.LogNormal

object Disease extends Dynamics {
  final val beta: Double = 0.3
  final val lastDay: Int = 12
}

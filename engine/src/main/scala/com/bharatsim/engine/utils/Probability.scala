package com.bharatsim.engine.utils

import scala.util.Random

object Probability {
  def toss(prob: Double, times: Int): Boolean = {
    for (_ <- 0 until times) {
      if (biasedCoinToss(prob)) return true
    }
    false
  }

  def biasedCoinToss(prob: Double): Boolean = {
    Random.nextDouble() < prob
  }
}

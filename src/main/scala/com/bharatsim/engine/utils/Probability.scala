package com.bharatsim.engine.utils

import scala.util.Random

/**
  * Utility to calculate probabilities
  */
object Probability {

  /**
    * Perform toss with given probability
    * @param prob probability for toss
    * @param times number of times to perform toss
    * @return result of the toss
    */
  def toss(prob: Double, times: Int): Boolean = {
    for (_ <- 0 until times) {
      if (biasedCoinToss(prob)) return true
    }
    false
  }

  /**
    * Perform biased toss
    * @param prob probability for toss
    * @return result of the toss
    */
  def biasedCoinToss(prob: Double): Boolean = {
    prob match{
      case 0.0 => false
      case 1.0 => true
      case _ => Random.nextDouble() < prob
    }
  }
}

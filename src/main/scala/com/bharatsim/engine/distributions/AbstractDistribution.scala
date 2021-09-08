package com.bharatsim.engine.distributions

import org.apache.commons.math3.distribution.AbstractRealDistribution

abstract class AbstractDistribution {

  protected var dist: AbstractRealDistribution
  /**
    * Draw random sample from the logNormal distribution
    * @return random sample
    */
  def sample(): Double = {
    dist.sample()
  }

  /**
    * Draw random sample from the logNormal distribution
    * @param size number of random samples to be returned
    * @return random sample of specific size
    */
  def sample(size: Int): Array[Double] = {
    if (size <= 0) {
      return Array.empty[Double]
    }
    dist.sample(size)
  }
}

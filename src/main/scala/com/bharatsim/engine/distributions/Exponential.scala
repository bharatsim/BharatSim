package com.bharatsim.engine.distributions

import org.apache.commons.math3.distribution.{AbstractRealDistribution, ExponentialDistribution}
import org.apache.commons.math3.random.{JDKRandomGenerator, RandomGenerator}

/**
  * Utility to create exponential distribution
  * @param mean mean of the distribution
  * @param randomGenerator randomGenerator, default value is JDKRandomGenerator()
  * @return Exponential distribution with given distributions mean
  */
case class Exponential(mean: Double, randomGenerator: RandomGenerator = new JDKRandomGenerator())
    extends AbstractDistribution {
  override protected var dist: AbstractRealDistribution = generateDistributionFrom(mean, randomGenerator)

  private def generateDistributionFrom(
      mean: Double,
      randomGenerator: RandomGenerator
  ): ExponentialDistribution = {

    new ExponentialDistribution(randomGenerator, mean)
  }
}

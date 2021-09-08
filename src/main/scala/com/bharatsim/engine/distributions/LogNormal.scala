package com.bharatsim.engine.distributions

import org.apache.commons.math3.distribution.{AbstractRealDistribution, LogNormalDistribution}
import org.apache.commons.math3.random.{JDKRandomGenerator, RandomGenerator}

import scala.math.{log, sqrt}

/**
  * Utility to create log-normal distribution
  * @param mean mean of the distribution
  * @param standardDeviation standard deviation of the distribution
  * @param randomGenerator randomGenerator, default value is JDKRandomGenerator()
  * @return LogNormal distribution with given distributions mean and standard deviation
  */
case class LogNormal(
    mean: Double,
    standardDeviation: Double,
    randomGenerator: RandomGenerator = new JDKRandomGenerator()
) extends AbstractDistribution {

  override protected var dist: AbstractRealDistribution =
    generateDistributionFrom(mean, standardDeviation, randomGenerator)

  private def generateDistributionFrom(
      mean: Double,
      standardDeviation: Double,
      randomGenerator: RandomGenerator
  ): LogNormalDistribution = {
    val scale: Double = log(mean) - 0.5 * log((standardDeviation * standardDeviation) / (mean * mean) + 1)
    val shape: Double = sqrt(log((standardDeviation * standardDeviation) / (mean * mean) + 1))

    new LogNormalDistribution(randomGenerator, scale, shape)
  }
}

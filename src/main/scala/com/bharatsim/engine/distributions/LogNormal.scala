package com.bharatsim.engine.distributions

import org.apache.commons.math3.distribution.LogNormalDistribution

import scala.math.{log, sqrt}

/**
  * Utility to create log-normal distribution
  * @param mean mean of the normal distribution
  * @param standardDeviation standard deviation of the normal distribution
  * @return LogNormal distribution with given normal distributions mean and standard deviation
  */
case class LogNormal(mean: Double, standardDeviation: Double) {

  private val dist: LogNormalDistribution = generateDistributionFrom(mean, standardDeviation)

  private def generateDistributionFrom(mean: Double, standardDeviation: Double): LogNormalDistribution = {
    val scale: Double = log(mean) - 0.5 * log((standardDeviation * standardDeviation) / (mean * mean) + 1)
    val shape: Double = sqrt(log((standardDeviation * standardDeviation) / (mean * mean) + 1))

    new LogNormalDistribution(scale, shape)
  }

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

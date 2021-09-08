package com.bharatsim.engine.distributions

import org.apache.commons.math3.random.JDKRandomGenerator
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec

class ExponentialTest extends AnyWordSpec with Matchers {
  "sample" when {
    "picked 100 numbers from distribution" should {
      "return true" in {
        val distribution = Exponential(4.6)
        distribution.sample(100).length shouldBe 100
      }

      "exponential distribution generated using seed" should {
        "return true" in {
          val expectedRandomNumbers = Array(2.11995873003953, 3.332048377538107, 13.532270368494418, 5.40772174922675,
            0.915911985482541, 0.02638830915475989, 12.76626534078365, 2.553271894512101, 3.8747368316976387,
            0.6889469184471588)
          val randomGenerator = new JDKRandomGenerator(10)
          val distribution = Exponential(4.6, randomGenerator)
          val sample = distribution.sample(10)

          sample shouldEqual expectedRandomNumbers
        }
      }
    }
  }
}

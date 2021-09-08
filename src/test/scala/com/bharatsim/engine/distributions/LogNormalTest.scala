package com.bharatsim.engine.distributions

import org.apache.commons.math3.random.JDKRandomGenerator
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec

class LogNormalTest extends AnyWordSpec with Matchers {
  "sample" when {
    "picked 100 numbers from distribution" should {
      "return true" in {
        val distribution = LogNormal(4.6, 4.8)
        distribution.sample(100).length shouldBe 100
      }
    }

    "logNormal distribution generated using seed" should {
      "return true" in {
        val expectedRandomNumbers = Array(6.742650643921753, 1.4458721403137953, 8.416157284428106, 2.1449145043423474,
          5.974796340338301, 4.654235638615424, 2.598950698059488, 4.731462994028174, 2.6967969343990754,
          3.1197194078914983)
        val randomGenerator = new JDKRandomGenerator(10)
        val distribution = LogNormal(4.6, 4.8, randomGenerator)

        distribution.sample(10) shouldEqual expectedRandomNumbers
      }
    }
  }

}

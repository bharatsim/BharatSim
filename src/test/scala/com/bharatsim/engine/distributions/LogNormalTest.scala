package com.bharatsim.engine.distributions

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
  }

}

package com.bharatsim.engine.utils

import com.bharatsim.engine.utils.Probability.{biasedCoinToss, toss}
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec

class ProbabilityTest extends AnyWordSpec with Matchers {
  "toss" when {
    "provided with probability as 1 with any times value greater than 0" should {
      "return true" in {
        toss(1.0, 1) shouldBe true
        toss(1.0, 10) shouldBe true
        toss(1.0, 45) shouldBe true
        toss(1.0, 100) shouldBe true
      }
    }

    "provided with probability as 0 with any times value greater than 0" should {
      "return false" in {
        toss(0.0, 1) shouldBe false
        toss(0.0, 10) shouldBe false
        toss(0.0, 45) shouldBe false
        toss(0.0, 100) shouldBe false
      }
    }

    "times is less than 1 regardless of probability value" should {
      "return false" in {
        toss(1.0, 0) shouldBe false
        toss(1.0, -10) shouldBe false
        toss(0.01, -8) shouldBe false
        toss(0.5, -3) shouldBe false
      }
    }
  }

  "biasedCoinToss" when {
    "given probability value as 1" should {
      "return true" in {
        biasedCoinToss(1.0) shouldBe true
      }
    }

    "given probability value as 0" should {
      "return false" in {
        biasedCoinToss(0) shouldBe false
      }
    }
  }
}

package com.bharatsim.engine.graph.patternMatcher

import com.bharatsim.engine.basicConversions.encoders.DefaultEncoders._
import com.bharatsim.engine.graph.patternMatcher.MatchCondition._
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec

class MatchConditionTest extends AnyWordSpec with Matchers {
  "equals condition" when {
    "parameter does not exist" should {
      "evaluate to false" in {
        val condition = "age" equ 34

        condition.eval(Map.empty) shouldBe false
      }
    }
  }

  "less than condition" when {
    "parameter does not exist" should {
      "evaluate to false" in {
        val condition = "age" lt 34

        condition.eval(Map.empty) shouldBe false
      }
    }
  }

  "greater than condition" when {
    "parameter does not exist" should {
      "evaluate to false" in {
        val condition = "age" gt 34

        condition.eval(Map.empty) shouldBe false
      }
    }
  }

  "less than equals condition" when {
    "parameter does not exist" should {
      "evaluate to false" in {
        val condition = "age" lte 34

        condition.eval(Map.empty) shouldBe false
      }
    }
  }

  "greater than equals condition" when {
    "parameter does not exist" should {
      "evaluate to false" in {
        val condition = "age" gte 34

        condition.eval(Map.empty) shouldBe false
      }
    }
  }
}

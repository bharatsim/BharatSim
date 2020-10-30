package com.bharatsim.engine.graph.patternMatcher

import com.bharatsim.engine.basicConversions.encoders.DefaultEncoders._
import com.bharatsim.engine.graph.patternMatcher.MatchCondition._
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec

class MatchPatternTest extends AnyWordSpec with Matchers {
  "Pattern" when {
    "evaluated" should {
      "return value of underlying expression" in {
        val pattern = Pattern("age" lte 30)

        pattern.eval(Map("age" -> 20)) shouldBe true
      }
    }
  }

  "AndPattern" when {
    "evaluated" should {
      "return and of values of underlying expressions" in {
        val pattern = AndPattern("age" lte 30, "name" equ "Ramesh")
        pattern.eval(Map("age" -> 20, "name" -> "Suresh")) shouldBe false

        val pattern1 = AndPattern("age" lte 30, "name" equ "Ramesh")
        pattern1.eval(Map("age" -> 20)) shouldBe false

        val pattern2 = AndPattern("age" lte 30, "name" equ "Ramesh")
        pattern2.eval(Map("age" -> 20, "name" -> "Ramesh")) shouldBe true
      }
    }
  }

  "OrPattern" when {
    "evaluated" should {
      "return or of values of underlying expressions" in {
        val pattern = OrPattern("age" lte 30, "name" equ "Ramesh")
        pattern.eval(Map("age" -> 20, "name" -> "Suresh")) shouldBe true

        val pattern1 = OrPattern("age" lte 18, "name" equ "Ramesh")
        pattern1.eval(Map("age" -> 20)) shouldBe false

        val pattern2 = OrPattern("age" lte 30, "name" equ "Ramesh")
        pattern2.eval(Map("age" -> 45, "name" -> "Suresh")) shouldBe false
      }
    }
  }
}

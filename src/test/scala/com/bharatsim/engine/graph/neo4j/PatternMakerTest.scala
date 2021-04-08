package com.bharatsim.engine.graph.neo4j

import com.bharatsim.engine.basicConversions.encoders.DefaultEncoders._
import com.bharatsim.engine.graph.patternMatcher.MatchCondition._
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec

class PatternMakerTest extends AnyWordSpec with Matchers {
  "from" should {
    "create pattern string with equal condition" in {
      val condition = "age" equ 30

      val patternWithParams = PatternMaker.from(condition, "a")
      patternWithParams.pattern shouldBe "a.age = $a0"
      patternWithParams.params shouldBe Map("a0" -> 30)
    }

    "create pattern string with less than condition" in {
      val condition = "age" lt 30

      val patternWithParams = PatternMaker.from(condition, "a")
      patternWithParams.pattern shouldBe "a.age < $a0"
      patternWithParams.params shouldBe Map("a0" -> 30)
    }

    "create pattern string with greater than condition" in {
      val condition = "age" gt 30

      val patternWithParams = PatternMaker.from(condition, "a")
      patternWithParams.pattern shouldBe "a.age > $a0"
      patternWithParams.params shouldBe Map("a0" -> 30)
    }

    "create pattern string with less than equals condition" in {
      val condition = "age" lte 30

      val patternWithParams = PatternMaker.from(condition, "a")
      patternWithParams.pattern shouldBe "a.age <= $a0"
      patternWithParams.params shouldBe Map("a0" -> 30)
    }

    "create pattern string with greater than equals condition" in {
      val condition = "age" gte 30

      val patternWithParams = PatternMaker.from(condition, "a")
      patternWithParams.pattern shouldBe "a.age >= $a0"
      patternWithParams.params shouldBe Map("a0" -> 30)
    }

    "create pattern string with pattern having and" in {
      val condition = "age" equ 30 and ("infectionState" equ "infected")

      val patternWithParams = PatternMaker.from(condition, "b")
      patternWithParams.pattern shouldBe "(b.age = $a0 AND b.infectionState = $b0)"
      patternWithParams.params shouldBe Map("a0" -> 30, "b0" -> "infected")
    }

    "create pattern string with pattern having or" in {
      val condition = "age" equ 30 or ("infectionState" equ "infected")

      val patternWithParams = PatternMaker.from(condition, "b")
      patternWithParams.pattern shouldBe "(b.age = $a0 OR b.infectionState = $b0)"
      patternWithParams.params shouldBe Map("a0" -> 30, "b0" -> "infected")
    }

    "create pattern string from pattern having both or and and" in {
      val condition = "age" equ 30 or ("infectionState" equ "infected") and ("income" lt 45000)

      val patternWithParams = PatternMaker.from(condition, "b")
      patternWithParams.pattern shouldBe "((b.age = $a0 OR b.infectionState = $b0) AND b.income < $c0)"
      patternWithParams.params shouldBe Map("a0" -> 30, "b0" -> "infected", "c0" -> 45000)
    }
  }
}

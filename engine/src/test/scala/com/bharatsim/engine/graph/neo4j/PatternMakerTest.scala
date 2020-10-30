package com.bharatsim.engine.graph.neo4j

import com.bharatsim.engine.basicConversions.encoders.DefaultEncoders._
import com.bharatsim.engine.graph.patternMatcher.MatchCondition._
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec

class PatternMakerTest extends AnyWordSpec with Matchers {
  "from" should {
    "create pattern string with equal condition" in {
      val condition = "age" equ 30

      PatternMaker.from(condition, "a") shouldBe "a.age = 30"
    }

    "create pattern string with less than condition" in {
      val condition = "age" lt 30

      PatternMaker.from(condition, "a") shouldBe "a.age < 30"
    }

    "create pattern string with greater than condition" in {
      val condition = "age" gt 30

      PatternMaker.from(condition, "a") shouldBe "a.age > 30"
    }

    "create pattern string with less than equals condition" in {
      val condition = "age" lte 30

      PatternMaker.from(condition, "a") shouldBe "a.age <= 30"
    }

    "create pattern string with greater than equals condition" in {
      val condition = "age" gte 30

      PatternMaker.from(condition, "a") shouldBe "a.age >= 30"
    }

    "create pattern string with pattern having and" in {
      val condition = "age" equ 30 and ("infectionState" equ "infected")

      PatternMaker.from(condition, "b") shouldBe """(b.age = 30 and b.infectionState = "infected")"""
    }

    "create pattern string with pattern having or" in {
      val condition = "age" equ 30 or ("infectionState" equ "infected")

      PatternMaker.from(condition, "b") shouldBe """(b.age = 30 or b.infectionState = "infected")"""
    }

    "create pattern string from pattern having both or and and" in {
      val condition = "age" equ 30 or ("infectionState" equ "infected") and ("income" lt 45000)

      PatternMaker.from(condition, "b") shouldBe """((b.age = 30 or b.infectionState = "infected") and b.income < 45000)"""
    }
  }
}

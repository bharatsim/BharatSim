package com.bharatsim.engine.models

import com.bharatsim.engine.graph.custom.IdGenerator
import org.scalatest.funsuite.AnyFunSuite
import org.scalatest.matchers.should.Matchers._

class IdGeneratorTest extends AnyFunSuite {

  test("should generate sequential Ids") {
    val idGenerator = new IdGenerator

    val id1 = idGenerator.generateId
    val id2 = idGenerator.generateId
    val id3 = idGenerator.generateId
    id1 shouldBe 1
    id2 shouldBe 2
    id3 shouldBe 3
  }

}

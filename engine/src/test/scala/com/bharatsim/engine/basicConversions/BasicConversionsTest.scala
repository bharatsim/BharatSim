package com.bharatsim.engine.basicConversions

import com.bharatsim.engine.basicConversions.decoders.BasicDecoder
import com.bharatsim.engine.basicConversions.decoders.DefaultDecoders._
import com.bharatsim.engine.basicConversions.encoders.BasicEncoder
import com.bharatsim.engine.basicConversions.encoders.DefaultEncoders._
import com.bharatsim.engine.testModels._
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec

class BasicConversionsTest extends AnyWordSpec with Matchers {
  "encode" when {
    "when model has basic types" should {
      "convert the model into map representation" in {
        val instance = TestCitizen(34)

        val value = BasicConversions.encode(instance)

        value shouldBe Map("age" -> 34)
      }
    }

    "when model has complex types and encoder is in implicit scope" should {
      "convert model into map with basic values" in {
        implicit val occupationEncoder: BasicEncoder[OccupationType] = BasicEncoder.instance({
          case Teacher => StringValue("Teacher")
          case Engineer => StringValue("Engineer")
        })
        val instance = ComplexModel("Raj", Teacher)

        val value = BasicConversions.encode(instance)

        value shouldBe Map("name" -> "Raj", "occupation" -> "Teacher")
      }
    }
  }

  "decode" when {
    "when model has basic types" should {
      "convert the map into model representation" in {
        val map = Map("age" -> 78)

        val value = BasicConversions.decode[TestCitizen](map)

        value shouldBe TestCitizen(78)
      }
    }

    "when model has complex types and decoder is in implicit scope" should {
      "convert map into model instance" in {
        implicit val occupationDecoder: BasicDecoder[OccupationType] = BasicDecoder.instance({
          case StringValue("Teacher") => Teacher
          case StringValue("Engineer") => Engineer
        })
        val map = Map("name" -> "Manish", "occupation" -> "Engineer")

        val value = BasicConversions.decode[ComplexModel](map)

        value shouldBe ComplexModel("Manish", Engineer)
      }
    }
  }
}

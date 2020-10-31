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
        val instance = SerdeExampleModel(
          isSmart = true,
          'C'.toByte,
          'F',
          "Ritika",
          25,
          155.4f,
          0.99d,
          564345L,
          List(12, 45),
          Map("some" -> 'v')
        )

        val value = BasicConversions.encode(instance)

        value("isSmart") shouldBe true
        value("singleByte") shouldBe 'C'
        value("gender") shouldBe 'F'
        value("name") shouldBe "Ritika"
        value("age") shouldBe 25
        value("height") shouldBe 155.4f
        value("prob") shouldBe 0.99d
        value("bankBalance") shouldBe 564345L
        value("l").asInstanceOf[List[Int]] should contain theSameElementsAs List(12, 45)
        value("m").asInstanceOf[Map[String, Char]]("some") shouldBe 'v'
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
        val map = Map(
          "name" -> "Ritika",
          "singleByte" -> 'C'.toByte,
          "prob" -> 0.99d,
          "bankBalance" -> 564345L,
          "height" -> 155.4F,
          "m" -> Map("some" -> 'v'),
          "l" -> List(12, 45),
          "gender" -> 'F',
          "isSmart" -> true,
          "age" -> 25
        )

        val value = BasicConversions.decode[SerdeExampleModel](map)

        value shouldBe SerdeExampleModel(
          isSmart = true,
          'C'.toByte,
          'F',
          "Ritika",
          25,
          155.4f,
          0.99d,
          564345L,
          List(12, 45),
          Map("some" -> 'v')
        )
      }
    }
  }
}

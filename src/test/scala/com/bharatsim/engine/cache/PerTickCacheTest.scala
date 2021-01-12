package com.bharatsim.engine.cache

import org.mockito.Mockito.{times, verify}
import org.mockito.MockitoSugar
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec

import scala.collection.mutable

class PerTickCacheTest extends AnyWordSpec with Matchers {
  "put" should {
    "add a value with key in cache" in {
      val perTickCache = new PerTickCache

      perTickCache.put("test", 23)

      perTickCache.get("test") shouldBe Some(23)
    }
  }

  "get" should {
    "return value if present" in {
      val perTickCache = new PerTickCache
      perTickCache.put("test", 23)

      perTickCache.get("test") shouldBe Some(23)
    }

    "return none if key not present" in {
      val perTickCache = new PerTickCache
      perTickCache.put("key", 23)

      perTickCache.get("test") shouldBe None
    }
  }

  "getOrElse" when {
    "key is present in cache" should {
      "return value from the store" in {
        val perTickCache = new PerTickCache(mutable.HashMap("key" -> "value"))
        val mockValueFunction = MockitoSugar.spyLambda(() => "value2")

        val returnedValue = perTickCache.getOrUpdate("key", mockValueFunction)

        returnedValue shouldBe "value"
        verify(mockValueFunction, times(0)).apply()
      }
    }

    "key is absent in cache" should {
      "evaluate value from valueFunction and return it" in {
        val perTickCache = new PerTickCache()
        val mockValueFunction = MockitoSugar.spyLambda(() => "value2")

        val returnedValue = perTickCache.getOrUpdate("key", mockValueFunction)

        returnedValue shouldBe "value2"
        verify(mockValueFunction, times(1)).apply()
      }
    }
  }

  "erase" should {
    "remove associated value if key present in cache" in {
      val perTickCache = new PerTickCache(mutable.HashMap("key" -> 23))

      perTickCache.erase("key")

      perTickCache.get("key") shouldBe None
    }
  }

  "clear" should {
    "remove all the keys and associated values from the store" in {
      val perTickCache = new PerTickCache(mutable.HashMap("key" -> 23, "test" -> "value"))

      perTickCache.clear()

      perTickCache.get("key") shouldBe None
      perTickCache.get("test") shouldBe None
    }
  }
}

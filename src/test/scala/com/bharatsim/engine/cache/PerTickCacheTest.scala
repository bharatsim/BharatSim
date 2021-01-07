package com.bharatsim.engine.cache

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

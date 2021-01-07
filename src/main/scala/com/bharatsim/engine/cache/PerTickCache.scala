package com.bharatsim.engine.cache

import scala.collection.mutable

class PerTickCache(store: mutable.HashMap[String, Any] = mutable.HashMap.empty[String, Any]) {
  def put(key: String, value: Any): Unit = {
    store.put(key, value)
  }

  def get(key: String): Option[Any] = {
    store.get(key)
  }

  def erase(key: String): Unit = {
    store.remove(key)
  }

  private[engine] def clear(): Unit = {
    store.clear()
  }
}

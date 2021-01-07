package com.bharatsim.engine.cache

import scala.collection.mutable

class PerTickCache(store: mutable.HashMap[Any, Any] = mutable.HashMap.empty) {
  def put(key: Any, value: Any): Unit = {
    store.put(key, value)
  }

  def get(key: Any): Option[Any] = {
    store.get(key)
  }

  def erase(key: Any): Unit = {
    store.remove(key)
  }

  private[engine] def clear(): Unit = {
    store.clear()
  }
}

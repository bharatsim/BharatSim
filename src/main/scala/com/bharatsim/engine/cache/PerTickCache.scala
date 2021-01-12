package com.bharatsim.engine.cache

import scala.collection.mutable

class PerTickCache(store: mutable.HashMap[Any, Any] = mutable.HashMap.empty) {
  def getOrUpdate(key: Any, valueFunction: () => Any): Any = {
    if (store.contains(key)) store(key)
    else {
      val value = valueFunction()
      put(key, value)
      value
    }
  }

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

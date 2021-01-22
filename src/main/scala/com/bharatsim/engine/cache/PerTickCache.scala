package com.bharatsim.engine.cache

import scala.collection.mutable

class PerTickCache(private val store: mutable.AbstractMap[Any, Any] = mutable.HashMap.empty) {
  /**
   * This function tries to retrieve value from the cache
   * If value is absent from the cache, it evaluates the valueFunction and puts the value in cache
   *
   * @param key           Key for which value to be fetched
   * @param valueFunction Function that returns value if value not present in cache
   * @return Value from either cache or the function
   */
  def getOrUpdate(key: Any, valueFunction: () => Any): Any = {
    if (store.contains(key)) store(key)
    else {
      val value = valueFunction()
      put(key, value)
      value
    }
  }

  /**
   * Puts value in cache
   *
   * @param key   Key for which value needs to be put
   * @param value value against the key
   */
  def put(key: Any, value: Any): Unit = {
    store.put(key, value)
  }

  /**
   * If key is present returns Some(value) else None
   *
   * @param key Key for which value needs to be fetched
   * @return Optional value
   */
  def get(key: Any): Option[Any] = {
    store.get(key)
  }

  /**
   * Erases the value from the store
   *
   * @param key key for which value needs to be erased
   */
  def erase(key: Any): Unit = {
    store.remove(key)
  }

  private[engine] def clear(): Unit = {
    store.clear()
  }
}

package com.bharatsim.engine

import scala.collection.mutable

private[engine] class Collection[T <: Identity] {
  private val collection: mutable.HashMap[Int, T] = new mutable.HashMap();

  def getAll(): Iterator[T] = {
    return collection.valuesIterator
  }

  def get(id: Int): T = {
    return collection.apply(id)
  }
  def add(value: T): Unit = {
    collection.addOne(value.id, value)
  }
}

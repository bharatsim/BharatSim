package com.bharatsim.engine

import scala.collection.mutable

private[engine] class Collection[T <: Identity] {
  private val collection: mutable.HashMap[Int, T] = new mutable.HashMap()

  def getAll: Iterator[T] = collection.valuesIterator

  def get(id: Int): T = collection(id)

  def add(value: T): Unit = collection.addOne(value.id, value)
}

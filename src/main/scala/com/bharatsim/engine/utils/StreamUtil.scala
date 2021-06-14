package com.bharatsim.engine.utils
import java.util.Spliterators
import java.util.stream.{Stream, StreamSupport}

import scala.jdk.CollectionConverters.IteratorHasAsJava
object StreamUtil {

  /**
    * @param iterator The iterator for the source
    * @param parallel if `true` then the returned stream is a parallel stream;
    *                 if `false` then returned stream is a sequential stream.
    * @tparam T Type of elements
    * @return a new sequential or parallel {@code Stream}
    */
  def create[T](iterator: Iterator[T], parallel: Boolean): Stream[T] = {
    StreamSupport.stream(Spliterators.spliteratorUnknownSize(iterator.asJava, 0), parallel)
  }
}

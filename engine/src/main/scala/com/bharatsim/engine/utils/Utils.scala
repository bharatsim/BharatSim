package com.bharatsim.engine.utils

import scala.reflect.{ClassTag, classTag}

object Utils {
  def fetchClassName[T: ClassTag]: String = {
    val className = classTag[T].runtimeClass.getName
    val label = className.split('.').last
    label
  }
}

package com.bharatsim.engine.utils

import scala.reflect.{ClassTag, classTag}

object Utils {
  def fetchClassName[T: ClassTag]: String = {
    classTag[T].runtimeClass.getSimpleName
  }
}

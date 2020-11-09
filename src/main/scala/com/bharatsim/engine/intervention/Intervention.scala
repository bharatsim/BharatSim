package com.bharatsim.engine.intervention

import com.bharatsim.engine.Context

trait Intervention {
  def name: String

  def shouldActivate(context: Context): Boolean

  def shouldDeactivate(context: Context): Boolean

  private[engine] def firstTimeAction(context: Context): Unit

  private[engine] def whenActiveAction(context: Context): Unit
}

object Intervention {
  def apply(
      interventionName: String,
      startConditionFunc: Context => Boolean,
      endConditionFunc: Context => Boolean,
      firstTimeActionFunc: Context => Unit = _ => {},
      whenActiveActionFunc: Context => Unit = _ => {}
  ): Intervention =
    new Intervention {
      override def name: String = interventionName

      override def shouldActivate(context: Context): Boolean = startConditionFunc(context)

      override def firstTimeAction(context: Context): Unit = firstTimeActionFunc(context)

      override def whenActiveAction(context: Context): Unit = whenActiveActionFunc(context)

      override def shouldDeactivate(context: Context): Boolean = endConditionFunc(context)
    }
}

package com.bharatsim.engine.intervention

import com.bharatsim.engine.Context

trait OffsetBasedIntervention extends Intervention {
  private[engine] var startedAt = 0
}

object OffsetBasedIntervention {
  def apply(
      interventionName: String,
      shouldActivateWhen: Context => Boolean,
      endAfterNTicks: Int,
      firstTimeActionFunc: Context => Unit = _ => {},
      whenActiveActionFunc: Context => Unit = _ => {}
  ): OffsetBasedIntervention =
    new OffsetBasedIntervention {
      override def name: String = interventionName

      override def shouldActivate(context: Context): Boolean = shouldActivateWhen(context)

      override def shouldDeactivate(context: Context): Boolean = {
        startedAt + endAfterNTicks + 1 == context.getCurrentStep
      }

      override private[engine] def firstTimeAction(context: Context): Unit = {
        startedAt = context.getCurrentStep
        firstTimeActionFunc(context)
      }

      override private[engine] def whenActiveAction(context: Context): Unit = whenActiveActionFunc(context)
    }
}

package com.bharatsim.engine.intervention
import com.bharatsim.engine.Context

private[engine] trait IntervalBasedIntervention extends Intervention

object IntervalBasedIntervention {
  def apply(
      interventionName: String,
      startTick: Int,
      endTick: Int,
      firstTimeActionFunc: Context => Unit = _ => {},
      whenActiveActionFunc: Context => Unit = _ => {}
  ): IntervalBasedIntervention = {
    assert(startTick < endTick, "Start cannot be greater than end")

    new IntervalBasedIntervention {
      override def name: String = interventionName

      override def shouldActivate(context: Context): Boolean = context.getCurrentStep == startTick

      override def shouldDeactivate(context: Context): Boolean = context.getCurrentStep == endTick

      override private[engine] def firstTimeAction(context: Context): Unit = firstTimeActionFunc(context)

      override private[engine] def whenActiveAction(context: Context): Unit = whenActiveActionFunc(context)
    }
  }
}

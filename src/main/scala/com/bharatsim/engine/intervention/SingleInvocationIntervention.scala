package com.bharatsim.engine.intervention
import com.bharatsim.engine.Context

private[engine] trait SingleInvocationIntervention extends Intervention {
  private[engine] var invokedOnce = false
}

object SingleInvocationIntervention {
  def apply(
      interventionName: String,
      shouldActivateFunc: Context => Boolean,
      shouldDeactivateFunc: Context => Boolean,
      firstTimeActionFunc: Context => Unit = _ => {},
      whenActiveActionFunc: Context => Unit = _ => {}
  ): SingleInvocationIntervention =
    new SingleInvocationIntervention {
      override def name: String = interventionName

      override def shouldActivate(context: Context): Boolean = {
        if (invokedOnce) false
        else shouldActivateFunc(context)
      }

      override def shouldDeactivate(context: Context): Boolean = shouldDeactivateFunc(context)

      override private[engine] def firstTimeAction(context: Context): Unit = {
        invokedOnce = true
        firstTimeActionFunc(context)
      }

      override private[engine] def whenActiveAction(context: Context): Unit = whenActiveActionFunc(context)
    }
}

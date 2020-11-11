package com.bharatsim.engine.intervention
import com.bharatsim.engine.Context

private[engine] trait SingleInvocationIntervention extends Intervention {
  private[engine] var invokedOnce = false
}

/**`SingleInvocationIntervention` can be used to create intervention which executes only one time throughout the simulation
 *
 *
 * @example
 * {{{
 * val shouldActivateWhen = (context: Context) => context.getCurrentStep == 5
 * val shouldDeactivateWhen = (context: Context) => context.getCurrentStep == 10
 * val intervention = SingleInvocationIntervention(
 *                        "sampleIntervention",
 *                         shouldActivateWhen,
 *                         shouldDeactivateWhen
 *                    )
 * }}}
 */
object SingleInvocationIntervention {
  /**Creator method
   *
   * @param interventionName unique name of the intervention
   * @param shouldActivateFunc function which tells whether this intervention should be activated
   * @param shouldDeactivateFunc function which tells whether this intervention should be deactivated
   * @param firstTimeActionFunc optional function which will be executed at the start of the intervention
   * @param whenActiveActionFunc optional function which will be executed per tick when intervention is active
   * @return Intervention instance
   */
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

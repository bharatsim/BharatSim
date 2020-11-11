package com.bharatsim.engine.intervention

import com.bharatsim.engine.Context

private[engine] trait OffsetBasedIntervention extends Intervention {
  private[engine] var startedAt = 0
}

/**`OffsetBasedIntervention` can be used to create intervention which ends after n ticks
 *
 * Start of simulation is governed by `shouldActivateWhen` function
 *
 * @example
 * {{{
 * val shouldActivateWhen = (context: Context) => context.getCurrentStep == 5
 * val intervention = OffsetBasedIntervention("sampleIntervention", shouldActivateWhen, 10)
 * }}}
 */
object OffsetBasedIntervention {
  /** Creator method
   *
   * @param interventionName unique name of the intervention
   * @param shouldActivateWhen function which decides when should intervention be activated
   * @param endAfterNTicks offset n, after n ticks from the start tick simulation will end
   * @param firstTimeActionFunc optional function which gets executed when simulation starts
   * @param whenActiveActionFunc optional function which gets executed per tick when simulation is active
   * @return Intervention instance
   */
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

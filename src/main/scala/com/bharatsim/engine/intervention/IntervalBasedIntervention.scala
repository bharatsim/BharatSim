package com.bharatsim.engine.intervention
import com.bharatsim.engine.Context

private[engine] trait IntervalBasedIntervention extends Intervention

/**`IntervalBasedIntervention` can be used to create intervention which starts and ends at the specific ticks
 *
 *
 * @example
 * {{{
 * val intervention = OffsetBasedIntervention("sampleIntervention", 10, 15)
 * }}}
 */
object IntervalBasedIntervention {
  /**Creator method
   *
   * @param interventionName unique intervention name
   * @param startTick integer specifying start tick for intervention (inclusive), should not be greater than endTick
   * @param endTick integer specifying end tick for the intervention (exclusive, intervention will not be active at `endTick`)
   * @param firstTimeActionFunc optional function which gets executed when simulation starts
   * @param whenActiveActionFunc optional function which gets executed per tick when simulation is active
   * @return Intervention instance
   */
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

package com.bharatsim.engine.intervention

import com.bharatsim.engine.Context

/**Intervention can help you take specific actions based on the current state of the simulation
  *
  * Simulation engine checks per tick whether an inactive intervention can be activated based
  * on the provided `shouldActivate` method
  *
  * All active interventions are available for user to inspect through `Context` object
  */
trait Intervention {

  /**`name` defines unique name of the intervention
    *
    * Same `name` is returned to the user when user inspects active interventions
    * @return name of the intervention
    */
  def name: String

  /**@param context state of the simulation at any given tick
    * @return decision whether intervention should be activated
    */
  def shouldActivate(context: Context): Boolean

  /**@param context state of the simulation at any given tick
    * @return decision whether intervention should be deactivated
    */
  def shouldDeactivate(context: Context): Boolean

  /**This function gets executed only at the start of the intervention
    * @param context state of the simulation at any given tick
    */
  private[engine] def firstTimeAction(context: Context): Unit

  /**This function gets executed for all ticks at which this intervention is active
    * @param context state of the simulation at any given tick
    */
  private[engine] def whenActiveAction(context: Context): Unit
}

object Intervention {

  /**Creator method for intervention
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
  ): Intervention =
    new Intervention {
      override def name: String = interventionName

      override def shouldActivate(context: Context): Boolean = shouldActivateFunc(context)

      override def firstTimeAction(context: Context): Unit = firstTimeActionFunc(context)

      override def whenActiveAction(context: Context): Unit = whenActiveActionFunc(context)

      override def shouldDeactivate(context: Context): Boolean = shouldDeactivateFunc(context)
    }
}

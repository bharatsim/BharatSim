package com.bharatsim.engine.actions

import com.bharatsim.engine.Context

/**
  * Creates Action that is executed when condition is matched.
  * @param action [[Action]] to execute
  * @param condition condition to decide when action is performed.
  */
case class ConditionalAction(action: Action, condition: Context => Boolean)

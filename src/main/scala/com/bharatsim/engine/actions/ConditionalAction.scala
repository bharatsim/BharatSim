package com.bharatsim.engine.actions

import com.bharatsim.engine.Context

case class ConditionalAction(action: Action, condition: Context => Boolean)

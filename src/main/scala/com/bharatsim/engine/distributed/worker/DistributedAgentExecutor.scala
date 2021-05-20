package com.bharatsim.engine.distributed.worker

import com.bharatsim.engine.execution.control.{BehaviourControl, DistributeStateControl}

case class DistributedAgentExecutor(behaviourControl: BehaviourControl, stateControl: DistributeStateControl)

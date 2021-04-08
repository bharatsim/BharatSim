package com.bharatsim.engine.distributed

import com.bharatsim.engine.graph.GraphProvider.NodeId

case class ContextData(currentTick: Int, activeIntervention: Set[String]) extends CborSerializable

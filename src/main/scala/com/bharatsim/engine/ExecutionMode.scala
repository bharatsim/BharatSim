package com.bharatsim.engine

sealed trait ExecutionMode

case object NoParallelism extends ExecutionMode

case object CollectionBased extends ExecutionMode

case object ActorBased extends ExecutionMode

case object Distributed extends ExecutionMode

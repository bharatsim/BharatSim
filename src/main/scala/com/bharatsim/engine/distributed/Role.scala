package com.bharatsim.engine.distributed

object Role extends Enumeration {
  type Role = Value

  val EngineMain, Worker = Value
}

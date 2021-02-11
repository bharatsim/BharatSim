package com.bharatsim.engine.distributed

object Role extends Enumeration {
  type Role = Value

  val DataStore, EngineMain, Worker = Value
}

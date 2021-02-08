package com.bharatsim.engine.distributed

import akka.actor.typed.Behavior
import akka.actor.typed.receptionist.{Receptionist, ServiceKey}
import akka.actor.typed.scaladsl.Behaviors
import com.bharatsim.engine.distributed.store.ActorBasedStore
import com.bharatsim.engine.distributed.store.ActorBasedStore.DBQuery

object Store {
  val storeServiceKey: ServiceKey[DBQuery] = ServiceKey[DBQuery]("StoreService")

  def setup(): Behavior[Nothing] = Behaviors.setup[Nothing](context => {
    val actorBasedStore = context.spawn(ActorBasedStore(), "store")

    context.system.receptionist ! Receptionist.register(storeServiceKey, actorBasedStore)
    Behaviors.empty
  })
}

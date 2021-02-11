package com.bharatsim.engine.distributed

import akka.actor.typed.Behavior
import akka.actor.typed.receptionist.{Receptionist, ServiceKey}
import akka.actor.typed.scaladsl.Behaviors
import akka.cluster.typed.Cluster
import com.bharatsim.engine.distributed.Role.DataStore
import com.bharatsim.engine.distributed.store.ActorBasedStore
import com.bharatsim.engine.distributed.store.ActorBasedStore.DBQuery

object Guardian {
  private val storeServiceKey: ServiceKey[DBQuery] = ServiceKey[DBQuery]("DataStore")

  def apply(): Behavior[Nothing] =
    Behaviors.setup[Nothing](context => {
      val cluster = Cluster(context.system)

      if (cluster.selfMember.hasRole(DataStore.toString)) {
        val actorBasedStore = context.spawn(ActorBasedStore(), "store")

        context.system.receptionist ! Receptionist.register(storeServiceKey, actorBasedStore)
      }

      Behaviors.empty
    })
}

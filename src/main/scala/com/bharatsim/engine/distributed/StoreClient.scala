package com.bharatsim.engine.distributed

import akka.actor.typed.receptionist.Receptionist
import akka.actor.typed.scaladsl.Behaviors
import akka.actor.typed.{ActorRef, ActorSystem, Behavior}
import com.bharatsim.engine.basicConversions.StringValue
import com.bharatsim.engine.distributed.Store.storeServiceKey
import com.bharatsim.engine.distributed.store.{ActorBasedGraphProvider, ActorBasedStore}
import com.bharatsim.engine.graph.GraphNode
import com.bharatsim.engine.graph.patternMatcher.{Equals, Pattern}

object StoreClient {
  def setup(): Behavior[Nothing] = Behaviors.setup[Nothing](context => {
    implicit val system: ActorSystem[_] = context.system

    system.receptionist ! Receptionist.Subscribe(Store.storeServiceKey, context.self.unsafeUpcast)

    Behaviors.receiveMessagePartial[Receptionist.Listing] {
      case storeServiceKey.Listing(listings) =>
        if(listings.nonEmpty)
          readData(listings.head)
        else println("Data service not found")
      Behaviors.same
    }.narrow
  })

  private def readData(value: ActorRef[ActorBasedStore.DBQuery])(implicit actorSystem: ActorSystem[_]): Unit = {
    val actorBasedGraphProvider = new ActorBasedGraphProvider(value)
    val personCount = actorBasedGraphProvider.fetchCount("Person", Pattern(Equals(StringValue("Ramesh"), "name")))
    println(s"person count is $personCount")

    val value1: Iterable[GraphNode] = actorBasedGraphProvider.fetchNodes("Person")
    println(s"Nodes: $value1")

    println(s"Ramesh node is: ${actorBasedGraphProvider.fetchNode("City", Map.empty)}")
  }
}

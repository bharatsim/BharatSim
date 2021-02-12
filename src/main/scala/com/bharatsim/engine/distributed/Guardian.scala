package com.bharatsim.engine.distributed

import akka.actor.typed.receptionist.{Receptionist, ServiceKey}
import akka.actor.typed.scaladsl.{ActorContext, Behaviors}
import akka.actor.typed.{ActorRef, Behavior}
import akka.cluster.typed.Cluster
import com.bharatsim.engine.Context
import com.bharatsim.engine.distributed.Role._
import com.bharatsim.engine.distributed.store.ActorBasedStore
import com.bharatsim.engine.distributed.store.ActorBasedStore.DBQuery
import com.bharatsim.engine.execution.AgentExecutor
import com.bharatsim.engine.execution.control.{BehaviourControl, StateControl}

object Guardian {
  private val storeServiceKey: ServiceKey[DBQuery] = ServiceKey[DBQuery]("DataStore")
  val workerServiceKey: ServiceKey[AgentProcessor.Command] = ServiceKey[AgentProcessor.Command]("Worker")

  def createWorker(context: ActorContext[Nothing], store: ActorRef[DBQuery], simulationContext: Context): Unit = {
    val behaviourControl = new BehaviourControl(simulationContext)
    val stateControl = new StateControl(simulationContext)
    val agentExecutor = new AgentExecutor(behaviourControl, stateControl)
    val worker = context.spawn(AgentProcessor(agentExecutor), "Worker");
    context.system.receptionist ! Receptionist.register(workerServiceKey, worker)
  }

  def createMain(context: ActorContext[Nothing], store: ActorRef[DBQuery], simulationContext: Context): Unit = {
    context.spawn(EnginMainActor(store, simulationContext), "EngineMain")
  }

  def apply(simulationContext: Context): Behavior[Nothing] =
    Behaviors.setup[Nothing](context => {
      val cluster = Cluster(context.system)

      if (cluster.selfMember.hasRole(DataStore.toString)) {
        val actorBasedStore = context.spawn(ActorBasedStore(), "store")

        context.system.receptionist ! Receptionist.register(storeServiceKey, actorBasedStore)
      }

      if (cluster.selfMember.hasRole(Worker.toString)) {
        context.system.receptionist ! Receptionist.Subscribe(storeServiceKey, context.self.unsafeUpcast)
        Behaviors
          .receiveMessagePartial[Receptionist.Listing] {
            case storeServiceKey.Listing(listings) =>
              if (listings.nonEmpty)
                createWorker(context, listings.head, simulationContext)
              else println("Data service not found")
              Behaviors.same
          }
          .narrow
      }

      if (cluster.selfMember.hasRole(EngineMain.toString)) {
        context.system.receptionist ! Receptionist.Subscribe(storeServiceKey, context.self.unsafeUpcast)
        Behaviors
          .receiveMessagePartial[Receptionist.Listing] {
            case storeServiceKey.Listing(listings) =>
              if (listings.nonEmpty)
                createMain(context, listings.head, simulationContext)
              else println("Data service not found")
              Behaviors.same
          }
          .narrow
      }

      Behaviors.empty
    })
}

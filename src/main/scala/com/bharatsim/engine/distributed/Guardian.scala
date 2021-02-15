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
import com.bharatsim.engine.graph.GraphProviderFactory

object Guardian {
  private val storeServiceKey: ServiceKey[DBQuery] = ServiceKey[DBQuery]("DataStore")
  val workerServiceKey: ServiceKey[DistributedAgentProcessor.Command] = ServiceKey[DistributedAgentProcessor.Command]("Worker")

  def createWorker(context: ActorContext[Nothing], store: ActorRef[DBQuery], simulationContext: Context): Unit = {
    val behaviourControl = new BehaviourControl(simulationContext)
    val stateControl = new StateControl(simulationContext)
    val agentExecutor = new AgentExecutor(behaviourControl, stateControl)
    context.spawn(SimulationContextSubscriber(simulationContext), "SimulationContextSubscriber");
    val worker = context.spawn(DistributedAgentProcessor(agentExecutor, simulationContext), "Worker");
    GraphProviderFactory.init(store, context.system)

    context.system.receptionist ! Receptionist.register(workerServiceKey, worker)
  }

  def createMain(context: ActorContext[Nothing], store: ActorRef[DBQuery], simulationContext: Context): Unit = {
    GraphProviderFactory.init(store, context.system)
    context.spawn(EngineMainActor(store, simulationContext), "EngineMain")
  }

  def apply(simulationContext: Context): Behavior[Nothing] =
    Behaviors.setup[Nothing](context => {
      val cluster = Cluster(context.system)

      if (cluster.selfMember.hasRole(DataStore.toString)) {
        val actorBasedStore = context.spawn(ActorBasedStore(), "store")
        GraphProviderFactory.initDataStore()
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

package com.bharatsim.engine.distributed

import akka.actor.typed.receptionist.{Receptionist, ServiceKey}
import akka.actor.typed.scaladsl.AskPattern.Askable
import akka.actor.typed.scaladsl.{ActorContext, Behaviors}
import akka.actor.typed.scaladsl.adapter._
import akka.actor.typed.{ActorRef, Behavior, Scheduler}
import akka.cluster.typed.Cluster
import akka.pattern.retry
import akka.util.Timeout
import com.bharatsim.engine.Context
import com.bharatsim.engine.distributed.Role._
import com.bharatsim.engine.distributed.store.ActorBasedStore
import com.bharatsim.engine.distributed.store.ActorBasedStore.DBQuery
import com.bharatsim.engine.execution.AgentExecutor
import com.bharatsim.engine.execution.control.{BehaviourControl, StateControl}
import com.bharatsim.engine.graph.GraphProviderFactory

import scala.concurrent.duration.{Duration, DurationInt}
import scala.concurrent.{Await, Future}

object Guardian {
  private val storeServiceKey: ServiceKey[DBQuery] = ServiceKey[DBQuery]("DataStore")
  val workerServiceKey: ServiceKey[DistributedAgentProcessor.Command] =
    ServiceKey[DistributedAgentProcessor.Command]("Worker")
  private var simulationContext: Context = null;
  private var actorContext: ActorContext[Nothing] = null;
  private var storeRef: ActorRef[DBQuery] = null;
  private var cluster: Cluster = null;

  private def getStoreRef(): Future[ActorRef[DBQuery]] = {
    val system = actorContext.system
    implicit val seconds: Timeout = 3.seconds
    implicit val scheduler: Scheduler = system.scheduler

    val storeList = Await.result(
      system.receptionist.ask[Receptionist.Listing]((replyTo) => Receptionist.find(storeServiceKey, replyTo)),
      Duration.Inf
    ) match {
      case storeServiceKey.Listing(listings) => listings
    }

    if (storeList.nonEmpty) {
      Future.successful(storeList.head)
    } else {
      Future.failed(new Exception("Data service not found"));
    }
  }

  private def awaitStoreRef(): ActorRef[DBQuery] = {
    implicit val scheduler = actorContext.system.scheduler.toClassic
    implicit val executionContext = actorContext.system.executionContext
    val retried = retry(() => getStoreRef(), 10, 100.milliseconds)
    Await.result(retried, Duration.Inf)
  }

  private def init(context: ActorContext[Nothing], simContext: Context): Unit = {
    actorContext = context
    simulationContext = simContext
    cluster = Cluster(actorContext.system)
    if (cluster.selfMember.hasRole(DataStore.toString)) {
      val actorBasedStore = actorContext.spawn(ActorBasedStore(), "store")
      storeRef = actorBasedStore
      GraphProviderFactory.initDataStore()
      actorContext.system.receptionist ! Receptionist.register(storeServiceKey, actorBasedStore)
    }

    if (cluster.selfMember.hasRole(Worker.toString) || cluster.selfMember.hasRole(EngineMain.toString)) {
      storeRef = awaitStoreRef()
      GraphProviderFactory.init(storeRef, context.system)
    }
  }

  def run(): Unit = {
    if (cluster.selfMember.hasRole(Worker.toString)) {
      createWorker(actorContext, storeRef, simulationContext)
    }

    if (cluster.selfMember.hasRole(EngineMain.toString)) {
      createMain(actorContext, storeRef, simulationContext)
    }
  }

  def createWorker(context: ActorContext[Nothing], store: ActorRef[DBQuery], simulationContext: Context): Unit = {
    val behaviourControl = new BehaviourControl(simulationContext)
    val stateControl = new StateControl(simulationContext)
    val agentExecutor = new AgentExecutor(behaviourControl, stateControl)
    context.spawn(SimulationContextSubscriber(simulationContext), "SimulationContextSubscriber");
    val worker = context.spawn(DistributedAgentProcessor(agentExecutor, simulationContext), "Worker");
    context.system.receptionist ! Receptionist.register(workerServiceKey, worker)
  }

  def createMain(context: ActorContext[Nothing], store: ActorRef[DBQuery], simulationContext: Context): Unit = {
    context.spawn(EngineMainActor(store, simulationContext), "EngineMain")
  }

  def apply(simulationContext: Context): Behavior[Nothing] =
    Behaviors.setup[Nothing](context => {
      init(context, simulationContext)
      Behaviors.empty
    })
}

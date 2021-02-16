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
  private var actorContext: ActorContext[Command] = null;
  private var storeRef: ActorRef[DBQuery] = null;
  private var cluster: Cluster = null;
  private var selfRef: ActorRef[Command] = null

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
    val retried = retry(() => getStoreRef(), 10, 1.seconds)
    Await.result(retried, Duration.Inf)
  }

  private def init(context: ActorContext[Command]): Unit = {
    actorContext = context
    cluster = Cluster(actorContext.system)
    if (cluster.selfMember.hasRole(DataStore.toString)) {
      val actorBasedStore = actorContext.spawn(ActorBasedStore(), "store")
      storeRef = actorBasedStore
      GraphProviderFactory.initDataStore()
      actorContext.system.receptionist ! Receptionist.register(storeServiceKey, actorBasedStore)

      context.log.info("Store Registerd")
    }

    if (cluster.selfMember.hasRole(Worker.toString) || cluster.selfMember.hasRole(EngineMain.toString)) {
      storeRef = awaitStoreRef()
      println("data store init path=====>", storeRef.path)

      GraphProviderFactory.init(storeRef, context.system)
    }
  }

  def run(simulationContext: Context): Unit = {
    selfRef ! Run(simulationContext)
  }

  def createWorker(context: ActorContext[Command], store: ActorRef[DBQuery], simulationContext: Context): Unit = {
    val behaviourControl = new BehaviourControl(simulationContext)
    val stateControl = new StateControl(simulationContext)
    val agentExecutor = new AgentExecutor(behaviourControl, stateControl)
    context.spawn(SimulationContextSubscriber(simulationContext), "SimulationContextSubscriber");
    val worker = context.spawn(DistributedAgentProcessor(agentExecutor, simulationContext), "Worker");
    context.system.receptionist ! Receptionist.register(workerServiceKey, worker)
    context.log.info("worker Registerd")

  }

  def createMain(context: ActorContext[Command], store: ActorRef[DBQuery], simulationContext: Context): Unit = {
    context.spawn(EngineMainActor(store, simulationContext), "EngineMain")
    context.log.info("Main staterd")

  }

  trait Command;
  case class Run(simContext: Context) extends Command

  def apply(): Behavior[Command] =
    Behaviors.setup[Command](context => {
      selfRef = context.self
      init(context)

      Behaviors.receiveMessage[Command] {
        case Run(simContext) => {
          if (cluster.selfMember.hasRole(Worker.toString)) {
            createWorker(context, storeRef, simContext)
          }

          if (cluster.selfMember.hasRole(EngineMain.toString)) {
            createMain(context, storeRef, simContext)
          }
          Behaviors.same
        }
      }
    })
}

package com.bharatsim.engine.distributed

import akka.actor.typed.scaladsl.AskPattern.Askable
import akka.actor.typed.{ActorRef, ActorSystem, Scheduler}
import akka.cluster.ddata.typed.scaladsl.Replicator._
import akka.cluster.ddata.typed.scaladsl.{DistributedData, Replicator}
import akka.cluster.ddata.{LWWMap, LWWMapKey, Replicator, SelfUniqueAddress}
import akka.util.Timeout
import com.bharatsim.engine.Context

import scala.concurrent.Await
import scala.concurrent.duration.{Duration, DurationInt}

object SimulationContextReplicator {

  val contextKey = "ReplicatedSimulationContext"
  case class ContextData(currentTick: Int, activeIntervention: Set[String]) extends CborSerializable

  private def fromContext(context: Context): ContextData = {
    ContextData(context.getCurrentStep, context.activeInterventionNames)
  }

  def updateContext(simulationContext: Context, system: ActorSystem[_]): Unit = {
    implicit val node: SelfUniqueAddress = DistributedData(system).selfUniqueAddress

    implicit val seconds: Timeout = 3.seconds
    implicit val scheduler: Scheduler = system.scheduler
    def dataKey: LWWMapKey[String, ContextData] = LWWMapKey(contextKey)
    val replicator = DistributedData
      .get(system)
      .replicator

    Await.result(
      replicator.ask((replyTo: ActorRef[UpdateResponse[LWWMap[String, ContextData]]]) =>
        Update(dataKey, LWWMap.empty[String, ContextData], WriteAll(3.seconds), replyTo)(
          _ :+ (contextKey -> fromContext(simulationContext))
        )
      ),
      Duration.Inf
    ) match {
      case res: UpdateResponse[LWWMap[String, ContextData]] => res
    }
  }
}

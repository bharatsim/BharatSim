package com.bharatsim.engine

import akka.actor.typed.ActorSystem
import com.bharatsim.engine.distributed.Guardian
import com.typesafe.config.ConfigFactory
import com.typesafe.scalalogging.LazyLogging

import scala.concurrent.{Await, ExecutionContext}
import scala.concurrent.duration.Duration.Inf
import scala.util.{Failure, Success}

class DistributedSimulation extends LazyLogging {
  private var ingestionStep: Context => Unit = _ => {}
  private var simulationBody: Context => Unit = _ => {}
  private var onComplete: Context => Unit = _ => {}

  def ingestData(f: (Context) => Unit): Unit = {
    ingestionStep = f
  }

  def defineSimulation(f: Context => Unit): Unit = {
    simulationBody = f
  }

  def run(): Unit = {
    val simulationDef = SimulationDefinition(ingestionStep, simulationBody, onComplete)
    val config = ConfigFactory.load("cluster")

    ActorSystem[Nothing](Guardian(simulationDef), "Cluster", config)
  }

  def onCompleteSimulation(f: Context => Unit): Unit = {
    onComplete = f
  }
}

case class SimulationDefinition(ingestionStep: Context => Unit, simulationBody: Context => Unit, onComplete: Context => Unit)

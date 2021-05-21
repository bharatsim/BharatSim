package com.bharatsim.engine.distributed

import akka.actor.testkit.typed.scaladsl.ActorTestKit
import akka.actor.typed.scaladsl.Behaviors
import akka.actor.typed.{ActorSystem, Behavior}
import com.bharatsim.engine.Context
import com.bharatsim.engine.distributed.Role._
import com.bharatsim.engine.distributed.engineMain.{DistributedTickLoop, EngineMainActor, WorkerCoordinator}
import com.bharatsim.engine.distributed.worker.WorkerActor
import com.bharatsim.engine.execution.SimulationDefinition
import com.typesafe.config.ConfigFactory
import org.mockito.Mockito.clearInvocations
import org.mockito.{ArgumentMatchersSugar, MockitoSugar}
import org.scalatest.funspec.AnyFunSpec
import org.scalatest.matchers.should.Matchers
import org.scalatest.{BeforeAndAfterAll, BeforeAndAfterEach}

import scala.concurrent.Await
import scala.concurrent.duration.Duration
import scala.jdk.CollectionConverters.{IterableHasAsJava, MapHasAsJava}

class GuardianTest
    extends AnyFunSpec
    with MockitoSugar
    with BeforeAndAfterAll
    with BeforeAndAfterEach
    with Matchers
    with ArgumentMatchersSugar {

  val mockFn = spyLambda((context: Context) => {})
  val simDef = SimulationDefinition(mockFn, mockFn, mockFn)

  val workerMonitor = spyLambda((simDef: SimulationDefinition) => {})
  class MockWorker extends WorkerActor {
    override def start(simDef: SimulationDefinition, context: Context): Behavior[WorkerActor.Command] = {
      Behaviors.setup { context =>
        workerMonitor(simDef)
        context.system.terminate()
        Behaviors.stopped
      }
    }
  }
  val engineMonitor = spyLambda((simDef: SimulationDefinition) => {})
  class MockEngine extends EngineMainActor {
    override def start(
        simulationDefinition: SimulationDefinition,
        system: ActorSystem[_],
        context: Context,
        workerCoordinator: WorkerCoordinator
    ): Behavior[DistributedTickLoop.Command] = {

      Behaviors.setup { context =>
        engineMonitor(simDef)
        context.system.terminate()
        Behaviors.stopped

      }
    }
  }
  val testConfigMap = Map("akka.actor.provider" -> "cluster", "akka.loglevel" -> "OFF")

  override def afterEach() = {
    clearInvocations(engineMonitor)
    clearInvocations(workerMonitor)
  }

  describe("Guardian") {

    it("should start worker when worker role is specified") {
      val configMap = testConfigMap.+(("akka.cluster.roles", List(Worker.toString).asJava))
      val config = ConfigFactory.parseMap(configMap.asJava)

      val testKit = ActorTestKit(config)
      val guardian = new Guardian(new MockEngine, new MockWorker)

      testKit.spawn[Nothing](guardian.start(simDef))

      Await.ready(testKit.system.whenTerminated, Duration.Inf)
      verify(workerMonitor)(simDef)
      verify(engineMonitor, never)(any)
      testKit.shutdownTestKit()
    }

    it("should start engineMain when engineMain role is specified") {
      val configMap = testConfigMap.+(("akka.cluster.roles", List(EngineMain.toString).asJava))
      val config = ConfigFactory.parseMap(configMap.asJava)
      val testKit = ActorTestKit(config)
      val guardian = new Guardian(new MockEngine, new MockWorker)

      testKit.spawn[Nothing](guardian.start(simDef))

      Await.ready(testKit.system.whenTerminated, Duration.Inf)
      verify(engineMonitor)(simDef)
      verify(workerMonitor, never)(any)
      testKit.shutdownTestKit()
    }
  }

}

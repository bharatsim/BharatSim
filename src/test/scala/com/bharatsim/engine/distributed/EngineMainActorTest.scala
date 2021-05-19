package com.bharatsim.engine.distributed

import akka.Done
import akka.actor.CoordinatedShutdown
import akka.actor.CoordinatedShutdown.Reason
import akka.actor.testkit.typed.scaladsl.{ActorTestKit, BehaviorTestKit}
import akka.actor.typed.ActorSystem
import com.bharatsim.engine.{ApplicationConfig, ApplicationConfigFactory, Context}
import com.bharatsim.engine.distributed.Guardian.UserInitiatedShutdown
import com.bharatsim.engine.distributed.engineMain.WorkerCoordinator
import com.bharatsim.engine.execution.SimulationDefinition
import com.bharatsim.engine.graph.GraphProviderFactory
import com.bharatsim.engine.graph.neo4j.BatchNeo4jProvider
import org.mockito.{ArgumentCaptor, ArgumentMatchersSugar, MockitoSugar}
import org.scalatest.{BeforeAndAfterAll, BeforeAndAfterEach}
import org.scalatest.funspec.AnyFunSpec
import org.scalatest.matchers.should.Matchers
import org.mockito.Mockito.clearInvocations

import scala.concurrent.duration.Duration
import scala.concurrent.{Await, ExecutionContext, Future}

class EngineMainActorTest
    extends AnyFunSpec
    with MockitoSugar
    with BeforeAndAfterAll
    with BeforeAndAfterEach
    with Matchers
    with ArgumentMatchersSugar {
  val testKit = ActorTestKit()

  val mockApplicationConfig = spy(new ApplicationConfig())
  ApplicationConfigFactory.testOverride(mockApplicationConfig)
  val mockGraphProvider = mock[BatchNeo4jProvider]
  GraphProviderFactory.testOverride(mockGraphProvider)
  val ingestionStep = spyLambda((context: Context) => {})
  val body = spyLambda((context: Context) => {})
  val onComplete = spyLambda((context: Context) => {})
  val mockWorkerCoordinator = mock[WorkerCoordinator]
  val simDef = SimulationDefinition(ingestionStep, body, onComplete)

  override def afterEach(): Unit = {
    clearInvocations(ingestionStep)
    clearInvocations(body)
    clearInvocations(onComplete)
    clearInvocations(mockWorkerCoordinator)
    reset(mockApplicationConfig)
  }

  override def afterAll() {
    testKit.shutdownTestKit()
  }
  describe("Engine Main Start") {

    it("should do ingestion and start tick loop") {

      val engineMain = new EngineMainActor().start(simDef, testKit.system, mockWorkerCoordinator)

      BehaviorTestKit(engineMain)

      val contextCaptor = ArgumentCaptor.forClass(classOf[Context])
      verify(ingestionStep)(contextCaptor.capture())
      val context = contextCaptor.getValue.asInstanceOf[Context]

      verify(body)(context)
      verify(onComplete, never)(any[Context])
      verify(mockWorkerCoordinator).initTick(any[ActorSystem[_]], eqTo(context), eqTo(List.empty))
      context.graphProvider shouldBe mockGraphProvider
    }

    it("should call simulation oncomplete on coordinated shutdown") {
      val actorTestKit = ActorTestKit()
      val engineMain = new EngineMainActor().start(simDef, actorTestKit.system, mockWorkerCoordinator)
      BehaviorTestKit(engineMain)
      val contextCaptor = ArgumentCaptor.forClass(classOf[Context])
      verify(ingestionStep)(contextCaptor.capture())
      val context = contextCaptor.getValue.asInstanceOf[Context]
      CoordinatedShutdown(actorTestKit.system).run(UserInitiatedShutdown)
      verify(onComplete)(context)
    }

    it("should not do ingestion when ingestion is disabled") {
      when(mockApplicationConfig.disableIngestion).thenReturn(true)
      val engineMain = new EngineMainActor().start(simDef, testKit.system, mockWorkerCoordinator)
      BehaviorTestKit(engineMain)
      verify(ingestionStep, never)(any[Context])
      verify(body)(any[Context])
    }

    it("should only do ingestion when ingestionOnly is set true ") {
      val actorTestKit = ActorTestKit()
      when(mockApplicationConfig.ingestionOnly).thenReturn(true)
      val coordinatedShutdownMonitor = spyLambda((reason: Reason) => "");
      val coordinatedShutdown = CoordinatedShutdown(actorTestKit.system)
      coordinatedShutdown
        .addTask(CoordinatedShutdown.PhaseBeforeServiceUnbind, "test") { () =>
          Future {
            coordinatedShutdownMonitor(coordinatedShutdown.getShutdownReason().get)
            Done
          }(ExecutionContext.global)
        }
      val engineMain = new EngineMainActor().start(simDef, actorTestKit.system, mockWorkerCoordinator)
      val mainActor = BehaviorTestKit(engineMain)
      Await.result(actorTestKit.system.whenTerminated, Duration.Inf)

      verify(coordinatedShutdownMonitor)(UserInitiatedShutdown)
      verify(ingestionStep)(any[Context])
      verify(body, never)(any[Context])
      mainActor.isAlive shouldBe false
    }
  }

}

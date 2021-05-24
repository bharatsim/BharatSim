package com.bharatsim.engine.execution.executors

import com.bharatsim.engine.{ActorBased, ApplicationConfig, CollectionBased, Distributed, NoParallelism}
import org.mockito.MockitoSugar
import org.scalatest.BeforeAndAfterEach
import org.scalatest.funsuite.AnyFunSuite
import org.scalatest.matchers.should.Matchers

class ExecutorFactoryTest extends AnyFunSuite with BeforeAndAfterEach with MockitoSugar with Matchers {

  val mockConfig = mock[ApplicationConfig]

  override def afterEach() = {
    reset(mockConfig)
  }

  test("should return distributed executor when execution mode is distributed") {

    when(mockConfig.executionMode).thenReturn(Distributed)

    new ExecutorFactory(mockConfig).getExecutor() shouldBe a[DistributedExecutor]
  }

  test("should return Actor Base executor when execution mode is ActorBased") {

    when(mockConfig.executionMode).thenReturn(ActorBased)

    new ExecutorFactory(mockConfig).getExecutor() shouldBe a[ActorBasedExecutor]
  }

  test("should return Default executor when execution mode is NoParallelism") {

    when(mockConfig.executionMode).thenReturn(NoParallelism)

    new ExecutorFactory(mockConfig).getExecutor() shouldBe a[DefaultExecutor]
  }

  test("should return Default executor when execution mode is CollectionBased ") {

    when(mockConfig.executionMode).thenReturn(CollectionBased)

    new ExecutorFactory(mockConfig).getExecutor() shouldBe a[DefaultExecutor]
  }
}

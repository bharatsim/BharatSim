package com.bharatsim.engine.execution.executors

import akka.actor.typed.scaladsl.Behaviors
import com.bharatsim.engine.distributed.Guardian
import com.bharatsim.engine.execution.SimulationDefinition
import org.mockito.MockitoSugar
import org.scalatest.funspec.AnyFunSpec
import org.scalatest.funsuite.AnyFunSuite

class DistributedExecutorTest extends AnyFunSuite with MockitoSugar {

  test("should start guardian actor") {
    val mockGuardian = mock[Guardian]
    val mockSimDef = mock[SimulationDefinition]
    when(mockGuardian.start(mockSimDef)).thenReturn(Behaviors.empty)
    new DistributedExecutor(mockGuardian).execute(mockSimDef)
    verify(mockGuardian).start(mockSimDef)

  }

}

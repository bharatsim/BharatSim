package com.bharatsim.engine.distributed.worker

import akka.actor.testkit.typed.scaladsl.ScalaTestWithActorTestKit
import com.bharatsim.engine.Context
import com.bharatsim.engine.ContextBuilder._
import com.bharatsim.engine.basicConversions.decoders.DefaultDecoders._
import com.bharatsim.engine.basicConversions.encoders.DefaultEncoders._
import com.bharatsim.engine.execution.control.{BehaviourControl, DistributeStateControl}
import com.bharatsim.engine.graph.neo4j.BatchNeo4jProvider
import com.bharatsim.engine.graph.{GraphNode, GraphProviderFactory}
import com.bharatsim.engine.testModels.TestFSM.IdleState
import com.bharatsim.engine.testModels.{StatefulPerson, Student}
import org.mockito.{ArgumentMatchersSugar, MockitoSugar}
import org.scalatest.funsuite.AnyFunSuiteLike

import scala.concurrent.Await
import scala.concurrent.duration.Duration

class DistributedAgentProcessorTest
    extends ScalaTestWithActorTestKit
    with AnyFunSuiteLike
    with MockitoSugar
    with ArgumentMatchersSugar {
  val mockGraphProvider = mock[BatchNeo4jProvider]
  GraphProviderFactory.testOverride(mockGraphProvider)
  test("should process given agent") {

    val statefulPersonNode = GraphNode("StatefulPerson", 1L, Map("name" -> "abc", "age" -> 1))
    val stateNode = GraphNode("IdleState", 2L, Map("idleFor" -> 1))
    val studentNode = GraphNode("Student", 3L)

    val agentsWithState = List((statefulPersonNode, Some(stateNode)), (studentNode, None))
    val agentProcessor = new DistributedAgentProcessor()
    implicit val context = Context()

    val statefulPerson = statefulPersonNode.as[StatefulPerson]
    val student = studentNode.as[Student]
    registerAgent[StatefulPerson]
    registerAgent[Student]
    registerState[IdleState]

    val mockBehaviourControl = mock[BehaviourControl]
    val mockStateControl = mock[DistributeStateControl]
    val agentExecutor = DistributedAgentExecutor(mockBehaviourControl, mockStateControl)
    val eventualDone = agentProcessor.process(agentsWithState, context, agentExecutor)

    Await.ready(eventualDone, Duration.Inf)
    verify(mockBehaviourControl).executeFor(statefulPerson)
    verify(mockBehaviourControl).executeFor(student)
    verify(mockStateControl).executeFor(statefulPerson)

  }

}

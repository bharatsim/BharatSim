package com.bharatsim.engine.control

import com.bharatsim.engine.basicConversions.decoders.BasicMapDecoder
import com.bharatsim.engine.basicConversions.decoders.DefaultDecoders._
import com.bharatsim.engine.basicConversions.encoders.BasicMapEncoder
import com.bharatsim.engine.basicConversions.encoders.DefaultEncoders._
import com.bharatsim.engine.cache.PerTickCache
import com.bharatsim.engine.fsm.State
import com.bharatsim.engine.graph.GraphProviderFactory
import com.bharatsim.engine.graph.custom.GraphProviderImpl
import com.bharatsim.engine.models.StatefulAgent
import com.bharatsim.engine.models.StatefulAgent.STATE_RELATIONSHIP
import com.bharatsim.engine.testModels.StatefulPerson
import com.bharatsim.engine.testModels.TestFSM.{IdleState, NoTransitionState, StateWithTransition}
import com.bharatsim.engine.{Context, Dynamics, SimulationConfig}
import org.mockito.MockitoSugar.mock
import org.scalatest.BeforeAndAfterEach
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec

import scala.reflect.ClassTag

class StateControlTest extends AnyWordSpec with BeforeAndAfterEach with Matchers {
  private val graph = new GraphProviderImpl()

  override protected def beforeEach(): Unit = {
    graph.deleteAll()
    GraphProviderFactory.testOverride(graph)
  }

  private def initializeStateAgent[T <: State : ClassTag](
                                                           withState: T
                                                         )(implicit encoder: BasicMapEncoder[T], decoder: BasicMapDecoder[T]): (StatefulAgent, Context) = {
    val context = new Context(graph, mock[Dynamics], mock[SimulationConfig], mock[PerTickCache])
    val agentToIngest = StatefulPerson("Shraddha", 33)
    agentToIngest.setInitialState(withState)
    context.graphProvider.createNodeFromInstance(agentToIngest)

    val agentToReturn = context.graphProvider.fetchNode("StatefulPerson", Map("age" -> 33))

    (agentToReturn.get.as[StatefulPerson], context)
  }

  "executeFor" when {
    "no transition" should {
      "execute per tick action for active state" in {
        val (testAgent, context) = initializeStateAgent(NoTransitionState(0))
        val stateControl = new StateControl(context)

        stateControl.executeFor(testAgent)

        val connections = testAgent.getConnections(STATE_RELATIONSHIP).toList
        connections.size shouldBe 1
        val state = connections.head.as[NoTransitionState]
        state.perTickActionInvokedTimes shouldBe 1
      }
    }

    "transition" should {
      "perform the transition deleting current state and creating new state" in {
        val (testAgent, context) = initializeStateAgent(StateWithTransition())
        val stateControl = new StateControl(context)

        stateControl.executeFor(testAgent)

        val connections = testAgent.getConnections(STATE_RELATIONSHIP).toList
        connections.size shouldBe 1
        connections.head.as[IdleState].isInstanceOf[IdleState] shouldBe true
      }

      "force update new state on current agent post state change" in {
        val (testAgent, context) = initializeStateAgent(StateWithTransition())
        val stateControl = new StateControl(context)

        stateControl.executeFor(testAgent)

        val state = testAgent.activeState.asInstanceOf[IdleState]
        state.idleFor shouldBe 0
      }

      "perform enter action for new state" in {
        val (testAgent, context) = initializeStateAgent(StateWithTransition())
        val stateControl = new StateControl(context)

        stateControl.executeFor(testAgent)

        val maybeNode = graph.fetchNode("StatefulPerson", Map("age" -> 33))
        maybeNode.get.as[StatefulPerson].name shouldBe "Santosh"
      }

      "perform per tick action for new state" in {
        val (testAgent, context) = initializeStateAgent(StateWithTransition())
        val stateControl = new StateControl(context)

        stateControl.executeFor(testAgent)

        val updatedState = testAgent.getConnections(STATE_RELATIONSHIP).toList.head.as[IdleState]
        updatedState.idleFor shouldBe 1
      }
    }
  }
}

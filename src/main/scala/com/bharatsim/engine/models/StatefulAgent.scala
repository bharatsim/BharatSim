package com.bharatsim.engine.models

import com.bharatsim.engine.basicConversions.decoders.BasicMapDecoder
import com.bharatsim.engine.basicConversions.encoders.BasicMapEncoder
import com.bharatsim.engine.fsm.State
import com.bharatsim.engine.graph.{GraphNode, NodeWithSerializer}
import com.bharatsim.engine.models.StatefulAgent.STATE_RELATIONSHIP

import scala.reflect.ClassTag

trait StatefulAgent extends Agent {
  private[engine] var maybeInitialState: Option[NodeWithSerializer[_]] = None

  private[engine] def hasInitialState: Boolean = maybeInitialState.isDefined

  private var maybeActiveState: Option[State] = None

  /**
    * Fetches current state of the agent
    *
    * It is initialized using the initial state value set on creation of the Agent
    *
    * Fetching from data-store is lazy in nature
    *
    * @throws RuntimeException if the initial state is not set for the Agent
    */
  def activeState: State = {
    maybeActiveState match {
      case Some(value) => value
      case _ =>
        maybeActiveState = Some(fetchActiveState)
        maybeActiveState.get
    }
  }

  /**
    * Mandatory method call for an StatefulAgent on creation
    *
    * @param s            State to be set as initial state
    * @param serializer   is a basic encoder for the State of type T
    * @param deserializer is a basic decoder for the State of type T
    * @tparam T Type of the State
    */
  def setInitialState[T <: State: ClassTag](
      s: T
  )(implicit serializer: BasicMapEncoder[T], deserializer: BasicMapDecoder[T]): Unit = {

    maybeInitialState = Some(NodeWithSerializer(s, serializer))
  }

  private[engine] def forceUpdateActiveState(): Unit = {
    maybeActiveState = Some(fetchActiveState)
  }

  private[engine] def setActiveState(state: GraphNode): Unit = {
    maybeActiveState = Some(State.fetchDeserializer(state.label)(state))
  }

  private def fetchActiveState: State = {
    val states = getConnections(STATE_RELATIONSHIP).toList
    if (states.nonEmpty) {
      val state: GraphNode = states.head
      val deserializer = State.fetchDeserializer(state.label)
      deserializer(state)
    } else {
      throw new Exception("Something went wrong, each StatefulAgent must have one active state all the times")
    }
  }
}

private[engine] object StatefulAgent {
  val STATE_RELATIONSHIP = "FSM_STATE"
}

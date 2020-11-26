package com.bharatsim.engine.models

import com.bharatsim.engine.basicConversions.decoders.BasicMapDecoder
import com.bharatsim.engine.basicConversions.encoders.BasicMapEncoder
import com.bharatsim.engine.fsm.State
import com.bharatsim.engine.graph.GraphNode
import com.bharatsim.engine.models.StatefulAgent.STATE_RELATIONSHIP
import com.bharatsim.engine.utils.Utils

import scala.reflect.ClassTag

trait StatefulAgent extends Agent {
  private[engine] var initialState: Option[State] = None
  private[engine] var initialStateLabel: String = ""

  private[engine] def hasInitialState: Boolean = initialState.isDefined

  lazy val activeState: State = {
    val states = getConnections(STATE_RELATIONSHIP).toList
    if (states.nonEmpty) {
      val state: GraphNode = states.head
      val deserializer = State.deSerializers(state.label)
      deserializer(state)
    } else {
      val serializer = State.serializers(initialStateLabel)
      val props = serializer(initialState.get)
      val id = graphProvider.createNode(initialStateLabel, props)
      graphProvider.createRelationship(internalId, STATE_RELATIONSHIP, id)

      initialState.get
    }
  }

  protected def setInitialState[T <: State: ClassTag](
      s: T
  )(implicit serializer: BasicMapEncoder[T], deserializer: BasicMapDecoder[T]): Unit = {
    val className = Utils.fetchClassName[T]
    State.saveSerde(className)

    initialState = Some(s)
    initialStateLabel = className
  }
}

private[engine] object StatefulAgent {
  val STATE_RELATIONSHIP = "FSM_STATE"
}





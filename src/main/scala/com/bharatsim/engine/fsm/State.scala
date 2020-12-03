package com.bharatsim.engine.fsm

import com.bharatsim.engine.Context
import com.bharatsim.engine.basicConversions.BasicConversions.{decode, encode}
import com.bharatsim.engine.basicConversions.decoders.BasicMapDecoder
import com.bharatsim.engine.basicConversions.encoders.BasicMapEncoder
import com.bharatsim.engine.fsm.State.deSerializers
import com.bharatsim.engine.graph.GraphNode
import com.bharatsim.engine.graph.custom.IdGenerator
import com.bharatsim.engine.models.{Node, StatefulAgent}
import com.bharatsim.engine.utils.Utils

import scala.collection.mutable
import scala.collection.mutable.ListBuffer
import scala.reflect.ClassTag

trait State extends Node {
  private[engine] val transitions = ListBuffer.empty[Transition[_]]

  def enterAction(context: Context, agent: StatefulAgent): Unit = {}

  def perTickAction(context: Context, agent: StatefulAgent): Unit = {}

  def addTransition[T <: State: ClassTag](when: (Context, StatefulAgent) => Boolean, to: T)(implicit
      serializer: BasicMapEncoder[T],
      deserializer: BasicMapDecoder[T]
  ): Unit = {
    val className = Utils.fetchClassName[T]
    deSerializers.put(className, graphNode => decode(graphNode.getParams))

    val transition = Transition(when, Left(to))
    transitions.addOne(transition)
  }

  def addTransition[T <: State: ClassTag](when: (Context, StatefulAgent) => Boolean, to: Context => T)(implicit
                                                                           serializer: BasicMapEncoder[T],
                                                                           deserializer: BasicMapDecoder[T]
  ): Unit = {
    val className = Utils.fetchClassName[T]
    deSerializers.put(className, graphNode => decode(graphNode.getParams))

    val transition = Transition(when, Right(to))
    transitions.addOne(transition)
  }
}

private[engine] object State {
  def saveSerde[T <: State](className: String)(implicit serializer: BasicMapEncoder[T], deserializer: BasicMapDecoder[T]): Option[State => Map[String, Any]] = {
    deSerializers.put(className, graphNode => decode(graphNode.getParams))
    serializers.put(className, v => encode(v.asInstanceOf[T]))
  }
  val deSerializers: mutable.HashMap[String, GraphNode => State] = mutable.HashMap.empty
  val serializers: mutable.HashMap[String, State => Map[String, Any]] = mutable.HashMap.empty

  val idGenerator = new IdGenerator()
}

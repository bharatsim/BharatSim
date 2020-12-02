package com.bharatsim.engine.fsm

import com.bharatsim.engine.Context
import com.bharatsim.engine.basicConversions.BasicConversions.encode
import com.bharatsim.engine.basicConversions.encoders.BasicMapEncoder
import com.bharatsim.engine.models.StatefulAgent
import com.bharatsim.engine.utils.Utils.fetchClassName

import scala.reflect.ClassTag

private[engine] case class Transition[T <: State: ClassTag](
    when: (Context, StatefulAgent) => Boolean,
    to: Either[T, Context => T]
)(implicit
    serializer: BasicMapEncoder[T]
) {

  def state(context: Context): State =
    to match {
      case Left(value)  => value
      case Right(value) => value(context)
    }

  def serializedState(state: Any): Map[String, Any] = {
    encode(state.asInstanceOf[T])
  }

  def label: String = fetchClassName[T]
}

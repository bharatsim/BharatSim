package akka

import akka.actor.typed.internal.AdaptWithRegisteredMessageAdapter
//TODO: Raise issue with akka for allowing to assert on msg AdaptWithRegisteredMessageAdapter
case class MockAdapterMsg[T](private val msg: Any) {
  def getMessage(): T = msg.asInstanceOf[AdaptWithRegisteredMessageAdapter[T]].msg;
}

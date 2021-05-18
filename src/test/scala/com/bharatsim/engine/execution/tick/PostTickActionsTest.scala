package com.bharatsim.engine.execution.tick

import com.bharatsim.engine.Context
import com.bharatsim.engine.actions.{Action, ConditionalAction}
import com.bharatsim.engine.execution.actions.PostTickActions
import org.mockito.MockitoSugar.{mock, spyLambda, verify, when}
import org.scalatest.wordspec.AnyWordSpec

import scala.collection.mutable.ListBuffer

class PostTickActionsTest extends AnyWordSpec {
  "execute" should {
    "check for conditional actions and if condition is satisfied, perform the action" in {
      implicit val context: Context = mock[Context]
      val action = mock[Action]
      val condition = spyLambda((_: Context) => true)
      val conditionalAction = ConditionalAction(action, condition)
      when(context.actions).thenReturn(ListBuffer(conditionalAction))

      new PostTickActions(context).execute()

      verify(condition).apply(context)
      verify(action).perform(context)
    }
  }
}

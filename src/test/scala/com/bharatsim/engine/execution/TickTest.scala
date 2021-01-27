package com.bharatsim.engine.execution

import com.bharatsim.engine._
import com.bharatsim.engine.execution.NodeWithDecoder.GenericNodeWithDecoder
import com.bharatsim.engine.execution.tick.{PostTickActions, PreTickActions}
import org.mockito.MockitoSugar.{mock, verify, when}
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec

class TickTest extends AnyWordSpec with Matchers {
  "exec" should {
    "invoke execution of all registered agents" in {
      val agentExecutor = mock[AgentExecutor]
      val context = mock[Context]
      val node1 = mock[GenericNodeWithDecoder]
      val node2 = mock[GenericNodeWithDecoder]
      when(context.registeredNodesWithDecoder).thenReturn(List(node1, node2))

      val step = new Tick(1, context, mock[PreTickActions], agentExecutor, mock[PostTickActions])
      step.exec()

      verify(agentExecutor).execute(node1)
      verify(agentExecutor).execute(node2)
    }
  }

  "preStepActions" should {
    "invoke pre tick actions for current tick" in {
      implicit val context: Context = mock[Context]

      val preTickActions = mock[PreTickActions]
      new Tick(89, context, preTickActions, mock[AgentExecutor], mock[PostTickActions]).preStepActions()

      verify(preTickActions).execute(89)
    }
  }

  "postStepActions" should {
    "invoke post tick actions for current tick" in {
      implicit val context: Context = mock[Context]

      val postTickActions = mock[PostTickActions]
      new Tick(89, context, mock[PreTickActions], mock[AgentExecutor], postTickActions).postStepActions()

      verify(postTickActions).execute()
    }
  }
}

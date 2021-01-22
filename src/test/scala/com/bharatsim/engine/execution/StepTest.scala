package com.bharatsim.engine.execution

import com.bharatsim.engine.ContextBuilder.registerAgent
import com.bharatsim.engine._
import com.bharatsim.engine.actions.{Action, ConditionalAction}
import com.bharatsim.engine.basicConversions.decoders.DefaultDecoders._
import com.bharatsim.engine.cache.PerTickCache
import com.bharatsim.engine.execution.StepTest.{graphNodeStudent, statefulPerson}
import com.bharatsim.engine.execution.control.{BehaviourControl, StateControl}
import com.bharatsim.engine.graph.{GraphNode, GraphProvider}
import com.bharatsim.engine.intervention.{Intervention, Interventions}
import com.bharatsim.engine.testModels.{StatefulPerson, Student}
import org.mockito.MockitoSugar.{mock, spyLambda, verify, when}
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec

import scala.collection.mutable.ListBuffer

class StepTest extends AnyWordSpec with Matchers {
  "exec" should {
    "invoke behaviour of an agent" in {
      val graphProvider = {
        val gp = mock[GraphProvider]
        when(gp.fetchNodes("Student")).thenReturn(List(graphNodeStudent))
        gp
      }
      val behaviourControl = mock[BehaviourControl]
      val stateControl = mock[StateControl]
      implicit val context: Context = getContext(1, graphProvider)
      registerAgent[Student]

      val step = new Step(1, context, behaviourControl, stateControl)
      step.exec()

      verify(behaviourControl).executeFor(graphNodeStudent.as[Student])
    }

    "invoke state control for an agent" in {
      val graphProvider = {
        val gp = mock[GraphProvider]
        when(gp.fetchNodes("StatefulPerson")).thenReturn(List(statefulPerson))
        gp
      }
      val behaviourControl = mock[BehaviourControl]
      val stateControl = mock[StateControl]
      implicit val context: Context = getContext(1, graphProvider)
      registerAgent[StatefulPerson]

      val step = new Step(1, context, behaviourControl, stateControl)
      step.exec()

      verify(stateControl).executeFor(statefulPerson.as[StatefulPerson])
    }

    "invoke behaviours for all the registered agents" in {
      val graphProvider = {
        val gp = mock[GraphProvider]
        when(gp.fetchNodes("StatefulPerson")).thenReturn(List(statefulPerson))
        when(gp.fetchNodes("Student")).thenReturn(List(graphNodeStudent))
        gp
      }
      val behaviourControl = mock[BehaviourControl]
      val stateControl = mock[StateControl]
      implicit val context: Context = getContext(1, graphProvider)
      registerAgent[StatefulPerson]
      registerAgent[Student]

      val step = new Step(1, context, behaviourControl, stateControl)
      step.exec()

      verify(behaviourControl).executeFor(graphNodeStudent.as[Student])
      verify(behaviourControl).executeFor(statefulPerson.as[StatefulPerson])
      verify(stateControl).executeFor(statefulPerson.as[StatefulPerson])
    }
  }

  "preStepActions" should {
    "should mark interventions active when they satisfy the condition" in {
      implicit val context: Context = mock[Context]

      val dummyIntervention: Intervention = getIntervention
      when(dummyIntervention.shouldActivate(context)).thenAnswer((c: Context) => c.getCurrentStep == 1)
      when(dummyIntervention.shouldDeactivate(context)).thenReturn(false)
      val interventions = {
        val i = mock[Interventions]
        when(i.inactive).thenReturn(List(dummyIntervention))
        when(i.active).thenReturn(List())
        i
      }
      when(context.interventions).thenReturn(interventions)
      when(context.perTickCache).thenReturn(mock[PerTickCache])
      when(context.getCurrentStep).thenReturn(1)

      new Step(1, context, mock[BehaviourControl], mock[StateControl]).preStepActions()

      verify(interventions).markActive(dummyIntervention)
    }

    "mark interventions inactive when they satisfy the deactivate condition" in {
      implicit val context: Context = mock[Context]
      val dummyIntervention: Intervention = getIntervention
      when(dummyIntervention.shouldActivate(context)).thenAnswer((_: Context) => true)
      when(dummyIntervention.shouldDeactivate(context)).thenAnswer((_: Context) => true)
      when(context.getCurrentStep).thenReturn(1, 2)
      val interventions = new Interventions
      interventions.add(dummyIntervention)
      interventions.markActive(dummyIntervention)
      when(context.interventions).thenReturn(interventions)
      when(context.perTickCache).thenReturn(mock[PerTickCache])

      new Step(1, context, mock[BehaviourControl], mock[StateControl]).preStepActions()

      interventions.inactive.length shouldBe 1
      interventions.inactive.head shouldBe dummyIntervention
    }

    "execute start-time action for every activated simulation" in {
      implicit val context: Context = mock[Context]

      val dummyIntervention: Intervention = getIntervention
      when(dummyIntervention.shouldActivate(context)).thenAnswer((c: Context) => c.getCurrentStep == 1)
      when(dummyIntervention.shouldDeactivate(context)).thenReturn(false)
      val interventions = {
        val i = mock[Interventions]
        when(i.inactive).thenReturn(List(dummyIntervention))
        when(i.active).thenReturn(List())
        i
      }
      when(context.interventions).thenReturn(interventions)
      when(context.perTickCache).thenReturn(mock[PerTickCache])
      when(context.getCurrentStep).thenReturn(1)

      new Step(1, context, mock[BehaviourControl], mock[StateControl]).preStepActions()

      verify(dummyIntervention).firstTimeAction(context)
    }

    "execute active-action for every active simulation" in {
      implicit val context: Context = mock[Context]
      val dummyIntervention: Intervention = getIntervention
      when(dummyIntervention.shouldActivate(context)).thenAnswer((context: Context) => context.getCurrentStep == 1)
      when(dummyIntervention.shouldDeactivate(context)).thenReturn(false)
      val interventions = mock[Interventions]
      when(interventions.inactive).thenReturn(List(dummyIntervention))
      when(interventions.active).thenReturn(List(), List(dummyIntervention))
      when(context.interventions).thenReturn(interventions)
      when(context.perTickCache).thenReturn(mock[PerTickCache])
      when(context.getCurrentStep).thenReturn(1)

      new Step(1, context, mock[BehaviourControl], mock[StateControl]).preStepActions()

      verify(dummyIntervention).whenActiveAction(context)
    }

    "set current step at the start of every simulation step" in {
      val steps = 3
      implicit val context: Context = getContext(steps)

      new Step(2, context, mock[BehaviourControl], mock[StateControl]).preStepActions()

      context.getCurrentStep shouldBe 2
    }

    "clear the cache" in {
      val mockCache = mock[PerTickCache]
      val context = getContext(2, perTickCache = mockCache)

      new Step(1, context, mock[BehaviourControl], mock[StateControl]).preStepActions()

      verify(mockCache).clear()
    }
  }

  "postStepActions" should {
    "check for conditional actions and if condition is satisfied, perform the action" in {
      implicit val context: Context = mock[Context]
      val action = mock[Action]
      val condition = spyLambda((_: Context) => true)
      val conditionalAction = ConditionalAction(action, condition)
      when(context.actions).thenReturn(ListBuffer(conditionalAction))

      new Step(1, context, mock[BehaviourControl], mock[StateControl]).postStepActions()

      verify(condition).apply(context)
      verify(action).perform(context)
    }
  }

  private def getContext(
      steps: Int,
      mockGraphProvider: GraphProvider = mock[GraphProvider],
      perTickCache: PerTickCache = mock[PerTickCache]
  ) = {
    new Context(mockGraphProvider, new Dynamics, SimulationConfig(steps), perTickCache)
  }

  private def getIntervention: Intervention = {
    val mockIntervention = mock[Intervention]
    when(mockIntervention.name).thenReturn("DummyIntervention")
    mockIntervention
  }
}

object StepTest {
  val graphNodeStudent: GraphNode = GraphNode("Student", 1)

  val graphNodeEmployee: GraphNode = GraphNode("Employee", 2)

  val statefulPerson: GraphNode = GraphNode("StatefulPerson", 3, Map("name" -> "some person", "age" -> 45))
}

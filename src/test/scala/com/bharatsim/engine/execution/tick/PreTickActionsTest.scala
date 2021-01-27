package com.bharatsim.engine.execution.tick

import com.bharatsim.engine.cache.PerTickCache
import com.bharatsim.engine.graph.GraphProvider
import com.bharatsim.engine.intervention.{Intervention, Interventions}
import com.bharatsim.engine.{Context, Dynamics, SimulationConfig}
import org.mockito.MockitoSugar.{mock, verify, when}
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec

class PreTickActionsTest extends AnyWordSpec with Matchers {
  "execute" should {
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

      new PreTickActions(context).execute(1)

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

      new PreTickActions(context).execute(1)

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

      new PreTickActions(context).execute(1)

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

      new PreTickActions(context).execute(1)

      verify(dummyIntervention).whenActiveAction(context)
    }

    "set current step at the start of every simulation step" in {
      val steps = 3
      implicit val context: Context = getContext(steps)

      new PreTickActions(context).execute(2)

      context.getCurrentStep shouldBe 2
    }

    "clear the cache" in {
      val mockCache = mock[PerTickCache]
      val context = getContext(2, perTickCache = mockCache)

      new PreTickActions(context).execute(1)

      verify(mockCache).clear()
    }
  }

  private def getIntervention: Intervention = {
    val mockIntervention = mock[Intervention]
    when(mockIntervention.name).thenReturn("DummyIntervention")
    mockIntervention
  }

  private def getContext(
                          steps: Int,
                          mockGraphProvider: GraphProvider = mock[GraphProvider],
                          perTickCache: PerTickCache = mock[PerTickCache]
                        ) = {
    new Context(mockGraphProvider, new Dynamics, SimulationConfig(steps), perTickCache)
  }
}

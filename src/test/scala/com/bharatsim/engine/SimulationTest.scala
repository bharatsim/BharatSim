package com.bharatsim.engine

import com.bharatsim.engine.ContextBuilder.{registerAction, registerAgent, registerIntervention}
import com.bharatsim.engine.actions.StopSimulation
import com.bharatsim.engine.basicConversions.decoders.DefaultDecoders._
import com.bharatsim.engine.control.{BehaviourControl, StateControl}
import com.bharatsim.engine.graph.GraphProvider.NodeId
import com.bharatsim.engine.graph.{GraphNode, GraphProvider}
import com.bharatsim.engine.intervention.Intervention
import com.bharatsim.engine.listeners.{SimulationListener, SimulationListenerRegistry}
import com.bharatsim.engine.models.Agent
import org.mockito.{InOrder, Mockito, MockitoSugar}
import org.scalatest.BeforeAndAfterEach
import org.scalatest.funsuite.AnyFunSuite
import org.scalatest.matchers.should.Matchers

class SimulationTest extends AnyFunSuite with MockitoSugar with BeforeAndAfterEach with Matchers {

  import com.bharatsim.engine.SimulationTest._

  override def beforeEach(): Unit = {
    Mockito.clearInvocations(behav1)
    Mockito.clearInvocations(behav2)
    Mockito.clearInvocations(behav3)
  }

  test("should execute empty simulation") {
    implicit val context: Context = getContext(1)
    noException should be thrownBy Simulation.run()
  }

  test("should execute multiple behaviours of agent in order") {
    val graphProvider = {
      val gp = mock[GraphProvider]
      when(gp.fetchNodes("Student")).thenReturn(List(graphNodeStudent))
      gp
    }
    implicit val context: Context = getContext(1, graphProvider)
    registerAgent[Student]
    Simulation.run()

    val order: InOrder = inOrder(behav1, behav2)
    order.verify(behav1)(context)
    order.verify(behav2)(context)

  }

  test("should execute all Behaviours for multiple agents") {
    val graphProvider = {
      val gp = mock[GraphProvider]
      when(gp.fetchNodes("Student")).thenReturn(List(graphNodeStudent))
      when(gp.fetchNodes("Employee")).thenReturn(List(graphNodeEmployee))
      gp
    }
    implicit val context: Context = getContext(1, graphProvider)
    registerAgent[Student]
    registerAgent[Employee]

    Simulation.run()

    val order: InOrder = inOrder(behav1, behav2, behav3)
    order.verify(behav1)(context)
    order.verify(behav2)(context)
    order.verify(behav3)(context)
  }

  test("should set current step at the start of every simulation step and run all the steps") {
    val steps = 3
    implicit val context: Context = getContext(steps)

    Simulation.run()

    context.getCurrentStep shouldBe 3
  }

  test("should execute all Behaviours of all agents for specified number of steps") {
    val graphProvider = {
      val gp = mock[GraphProvider]
      when(gp.fetchNodes("Student")).thenReturn(
        List(mockGraphNode("Student", 1), mockGraphNode("Student", 2))
      )
      gp
    }
    val steps = 2
    implicit val context: Context = getContext(steps, graphProvider)

    registerAgent[Student]

    val order: InOrder = inOrder(
      _goToSchoolForStep,
      _playAGameForStep,
      _goToSchoolForStep,
      _playAGameForStep
    )

    Simulation.run()

    order.verify(_goToSchoolForStep)(1)
    order.verify(_playAGameForStep)(1)
    order.verify(_goToSchoolForStep)(1)
    order.verify(_playAGameForStep)(1)

    order.verify(_goToSchoolForStep)(2)
    order.verify(_playAGameForStep)(2)
    order.verify(_goToSchoolForStep)(2)
    order.verify(_playAGameForStep)(2)
  }

  test("should notify simulation listeners") {
    val mockListener = mock[SimulationListener]
    SimulationListenerRegistry.register(mockListener)
    implicit val context: Context = getContext(1)
    Simulation.run()
    val order = inOrder(mockListener)
    order.verify(mockListener).onSimulationStart(context)
    order.verify(mockListener).onStepStart(context)
    order.verify(mockListener).onStepEnd(context)
    order.verify(mockListener).onSimulationEnd(context)
  }

  test("should check for conditional actions and if condition is satisfied, perform the action") {
    val graphProvider = mock[GraphProvider]
    implicit val context: Context = getContext(2, graphProvider)

    registerAction(StopSimulation, c => c.getCurrentStep == 1)

    Simulation.run()

    verifyZeroInteractions(graphProvider)
    context.getCurrentStep shouldBe 1
  }

  def getIntervention: Intervention = {
    val mockIntervention = mock[Intervention]
    when(mockIntervention.name).thenReturn("DummyIntervention")
    mockIntervention
  }

  test("simulation should mark interventions active when they satisfy the condition") {
    implicit val context: Context = getContext(3)
    val simulation: Simulation = newSimulation(context)

    val dummyIntervention: Intervention = getIntervention
    when(dummyIntervention.shouldActivate(context)).thenAnswer((context: Context) => context.getCurrentStep == 1)
    when(dummyIntervention.shouldDeactivate(context)).thenReturn(false)

    registerIntervention(dummyIntervention)
    simulation.run()

    val interventionNames = context.activeInterventionNames
    interventionNames.size shouldBe 1
    interventionNames.head shouldBe "DummyIntervention"
  }

  test("simulation should mark interventions inactive when they satisfy the deactivate condition") {
    implicit val context: Context = getContext(3)
    val simulation: Simulation = newSimulation(context)
    val dummyIntervention: Intervention = getIntervention
    when(dummyIntervention.shouldActivate(context)).thenAnswer((context: Context) => context.getCurrentStep == 1)
    when(dummyIntervention.shouldDeactivate(context)).thenAnswer((context: Context) => context.getCurrentStep == 2)

    registerIntervention(dummyIntervention)
    simulation.run()

    context.interventions.inactive.size shouldBe 1
    context.interventions.inactive.head.name shouldBe "DummyIntervention"
  }

  test("simulation should execute start-time action for every activated simulation") {
    implicit val context: Context = getContext(3)
    val simulation: Simulation = newSimulation(context)
    val dummyIntervention: Intervention = getIntervention
    when(dummyIntervention.shouldActivate(context)).thenAnswer((context: Context) => context.getCurrentStep == 1)
    when(dummyIntervention.shouldDeactivate(context)).thenReturn(false)

    registerIntervention(dummyIntervention)
    simulation.run()

    verify(dummyIntervention, times(1)).firstTimeAction(context)
  }

  test("simulation should execute active-action for every active simulation per tick") {
    implicit val context: Context = getContext(3)
    val simulation = newSimulation(context)
    val dummyIntervention: Intervention = getIntervention
    when(dummyIntervention.shouldActivate(context)).thenAnswer((context: Context) => context.getCurrentStep == 1)
    when(dummyIntervention.shouldDeactivate(context)).thenReturn(false)

    registerIntervention(dummyIntervention)
    simulation.run()

    verify(dummyIntervention, times(3)).whenActiveAction(context)
  }

  private def getContext(steps: Int, mockGraphProvider: GraphProvider = mock[GraphProvider]) =
    new Context(mockGraphProvider, new Dynamics, SimulationConfig(steps))


  private def newSimulation(context: Context) = {
    val behaviourControl = new BehaviourControl(context)
    val stateControl = new StateControl(context)
    val simulation = new Simulation(context, behaviourControl, stateControl)
    simulation
  }
}

case class Employee() extends Agent {

  import com.bharatsim.engine.SimulationTest.behav3

  val goToOffice: Context => Unit = behav3
  addBehaviour(goToOffice)
}

case class Student() extends Agent {

  import com.bharatsim.engine.SimulationTest.{behav1, behav2}

  val goToSchool: Context => Unit = behav1

  val playAGame: Context => Unit = behav2
  addBehaviour(goToSchool)
  addBehaviour(playAGame)
}

object SimulationTest {

  import org.mockito.MockitoSugar.spyLambda

  val _goToSchoolForStep: NodeId => Unit = spyLambda[NodeId => Unit]((_: Int) => {})
  val _playAGameForStep: NodeId => Unit = spyLambda[NodeId => Unit]((_: Int) => {})

  val behav1: Context => Unit = spyLambda[Context => Unit]((context: Context) => {
    _goToSchoolForStep(context.getCurrentStep)
  })

  val behav2: Context => Unit = spyLambda[Context => Unit]((context: Context) => {
    _playAGameForStep(context.getCurrentStep)
  })

  val behav3: Context => Unit = spyLambda[Context => Unit]((_: Context) => {})

  val graphNodeStudent: GraphNode = mockGraphNode("Student", 1)

  val graphNodeEmployee: GraphNode = mockGraphNode("Employee", 2)

  private def mockGraphNode(name: String, id: Int) = {
    new GraphNode {
      override def label: String = name

      override def Id: NodeId = id

      override def apply(key: String): Option[Any] = None

      override def getParams: Map[String, Any] = Map.empty
    }
  }
}

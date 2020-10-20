package com.bharatsim.engine

import com.bharatsim.engine.basicConversions.decoders.DefaultDecoders._
import com.bharatsim.engine.graph.GraphProvider.NodeId
import com.bharatsim.engine.graph.{GraphNode, GraphProvider}
import com.bharatsim.engine.listners.{SimulationListener, SimulationListenerRegistry}
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
    implicit val context: Context = getContext()
    noException should be thrownBy Simulation.run()
  }

  test("should execute multiple behaviours of agent in order") {
    val graphProvider = {
      val gp = mock[GraphProvider]
      when(gp.fetchNodes("Student")).thenReturn(List(graphNodeStudent))
      gp
    }
    implicit val context: Context = getContext(graphProvider)
    context.registerAgent[Student]
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
    implicit val context: Context = getContext(graphProvider)
    context.registerAgent[Student]
    context.registerAgent[Employee]

    Simulation.run()

    val order: InOrder = inOrder(behav1, behav2, behav3)
    order.verify(behav1)(context)
    order.verify(behav2)(context)
    order.verify(behav3)(context)
  }

  test("should set current step at the start of every simulation step and run all the steps") {
    implicit val context: Context = getContext()

    val steps = 3
    context.simulationContext.setSteps(steps)

    Simulation.run()

    context.simulationContext.getCurrentStep shouldBe 3
  }

  test("should execute all Behaviours of all agents for specified number of steps") {
    val graphProvider = {
      val gp = mock[GraphProvider]
      when(gp.fetchNodes("Student")).thenReturn(
        List(mockGraphNode("Student", 1), mockGraphNode("Student", 2))
      )
      gp
    }
    implicit val context: Context = getContext(graphProvider)

    val steps = 2
    context.simulationContext.setSteps(steps)
    context.registerAgent[Student]

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
    implicit val context: Context = getContext()
    Simulation.run()
    val order = inOrder(mockListener)
    order.verify(mockListener).onSimulationStart(context)
    order.verify(mockListener).onStepStart(context)
    order.verify(mockListener).onStepEnd(context)
    order.verify(mockListener).onSimulationEnd(context)
  }

  def getContext(mockGraphProvider: GraphProvider = mock[GraphProvider]) =
    new Context(mockGraphProvider, new Dynamics, new SimulationContext())
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
    _goToSchoolForStep(context.simulationContext.getCurrentStep)
  })

  val behav2: Context => Unit = spyLambda[Context => Unit]((context: Context) => {
    _playAGameForStep(context.simulationContext.getCurrentStep)
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

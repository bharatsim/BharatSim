package com.bharatsim.engine.execution.executors

import com.bharatsim.engine.ContextBuilder.registerAgent
import com.bharatsim.engine.basicConversions.decoders.DefaultDecoders._
import com.bharatsim.engine.execution.actions._
import com.bharatsim.engine.execution.control.{BehaviourControl, StateControl}
import com.bharatsim.engine.execution.{AgentExecutor, SimulationDefinition}
import com.bharatsim.engine.graph.{GraphNode, GraphProvider}
import com.bharatsim.engine.models.Agent
import com.bharatsim.engine.testModels.Student
import com.bharatsim.engine.{ApplicationConfig, Context}
import org.mockito.{ArgumentMatchersSugar, Mockito, MockitoSugar}
import org.scalatest.BeforeAndAfterEach
import org.scalatest.funsuite.AnyFunSuite
import org.scalatest.matchers.should.Matchers

class DefaultExecutorTest
    extends AnyFunSuite
    with BeforeAndAfterEach
    with MockitoSugar
    with Matchers
    with ArgumentMatchersSugar {
  val graphNodeStudent: GraphNode = GraphNode("Student", 1L)

  val graphNodeEmployee: GraphNode = GraphNode("Employee", 2L)
  val mockGraphProvider = mock[GraphProvider]
  val mockConfig = mock[ApplicationConfig]
  val context = Context(mockGraphProvider, mockConfig)
  val simulationSteps = 2
  override def beforeEach(): Unit = {
    when(mockGraphProvider.fetchNodes("Student")).thenAnswer(List(graphNodeStudent).iterator)
    when(mockConfig.simulationSteps).thenReturn(simulationSteps)

  }
  override def afterEach() = {
    reset(mockConfig)
    reset(mockGraphProvider)
  }

  private def mockActions(): Actions = {
    val actions = mock[Actions]
    val preSim = mock[PreSimulationActions]
    val postSim = mock[PostSimulationActions]
    val preTick = mock[PreTickActions]
    val postTick = mock[PostTickActions]
    when(actions.preSimulation).thenReturn(preSim)
    when(actions.postSimulation).thenReturn(postSim)
    when(actions.preTick).thenReturn(preTick)
    when(actions.postTick).thenReturn(postTick)
    actions
  }

  private def prepareInOrder(simDef: SimulationDefinition, actions: Actions, behaviourControl: BehaviourControl) = {
    Mockito.inOrder(
      mockGraphProvider,
      simDef.ingestionStep,
      simDef.simulationBody,
      simDef.onComplete,
      actions.preSimulation,
      actions.postSimulation,
      actions.preTick,
      actions.postTick,
      behaviourControl
    )
  }

  private def mockExecutionContext() = {
    val stateControl = mock[StateControl]
    val behaviourControl = mock[BehaviourControl]
    val agentExecutor = new AgentExecutor(behaviourControl, stateControl)
    val actions = mockActions()
    val mockExecutorContext = mock[ExecutorContext]
    when(mockExecutorContext.prepare(any[Context])).thenReturn((agentExecutor, actions))
    (mockExecutorContext, actions, behaviourControl)
  }
  test("should execute steps provided in config") {
    def mockFn() = spyLambda((context: Context) => {})
    def mockBody =
      spyLambda((context: Context) => {
        implicit val c: Context = context
        registerAgent[Student]
      })
    val simDef = SimulationDefinition(mockFn(), mockBody, mockFn())
    val (mockExecutorContext, actions, behaviourControl) = mockExecutionContext()
    val student = graphNodeStudent.as[Student]

    new DefaultExecutor(mockExecutorContext, context).execute(simDef)

    val orderedVerify = prepareInOrder(simDef, actions, behaviourControl)
    orderedVerify.verify(mockGraphProvider).clearData()
    orderedVerify.verify(simDef.ingestionStep)(context)
    orderedVerify.verify(simDef.simulationBody)(context)
    orderedVerify.verify(actions.preSimulation).execute()
    orderedVerify.verify(actions.preTick).execute(1)
    orderedVerify.verify(behaviourControl).executeFor(student)
    orderedVerify.verify(actions.postTick).execute()
    orderedVerify.verify(actions.preTick).execute(2)
    orderedVerify.verify(behaviourControl).executeFor(student)
    orderedVerify.verify(actions.postTick).execute()
    orderedVerify.verify(actions.postSimulation).execute()
    orderedVerify.verify(simDef.onComplete)(context)

  }

  test("should stop sim when simulation is mark to stop") {
    def mockFn() = spyLambda((context: Context) => {})
    def mockBody =
      spyLambda((context: Context) => {
        context.stopSimulation = true
      })
    val simDef = SimulationDefinition(mockFn(), mockBody, mockFn())
    val (mockExecutorContext, actions, behaviourControl) = mockExecutionContext()

    new DefaultExecutor(mockExecutorContext, context).execute(simDef)

    val orderedVerify = prepareInOrder(simDef, actions, behaviourControl)
    orderedVerify.verify(simDef.ingestionStep)(context)
    orderedVerify.verify(simDef.simulationBody)(context)
    orderedVerify.verify(actions.preSimulation).execute()
    orderedVerify.verify(actions.postSimulation).execute()
    orderedVerify.verify(simDef.onComplete)(context)

    verify(actions.preTick, never).execute(any[Int])
    verify(actions.postTick, never).execute()
    verify(behaviourControl, never).executeFor(any[Agent])

  }

}

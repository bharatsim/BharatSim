package com.bharatsim.engine

import org.mockito.{InOrder, MockitoSugar}
import org.scalatest.funsuite.AnyFunSuite
import org.scalatest.matchers.should.Matchers._

class SimulationTest extends AnyFunSuite with MockitoSugar {
  class Employee extends Agent {
    val goToOffice = spyLambda((context: Context) => {})
    addBehaviour(goToOffice)
  }

  class Student extends Agent {
    val _goToSchoolForStep = spyLambda((step: Int) => {})
    val _playAGameForStep = spyLambda((step: Int) => {})

    val goToSchool = spyLambda((context: Context) => {
      _goToSchoolForStep(context.simulationContext.getCurrentStep())
    })
    val playAGame = spyLambda((context: Context) => {
      _playAGameForStep(context.simulationContext.getCurrentStep())
    })
    addBehaviour(goToSchool)
    addBehaviour(playAGame)
  }

  test("should execute empty simulation") {
    noException should be thrownBy Simulation.run(new Context)
  }

  test("should execute multiple behaviours of agent in order") {
    val context = new Context
    val student = new Student
    context.agents.add(student);

    Simulation.run(context)

    val order: InOrder = inOrder(student.playAGame, student.goToSchool);
    order.verify(student.goToSchool)(context)
    order.verify(student.playAGame)(context)

  }

  test("should execute all Behaviours for multiple agents") {
    val context = new Context
    val student = new Student
    val employee = new Employee
    context.agents.add(student)
    context.agents.add(employee)

    Simulation.run(context)

    verify(student.goToSchool)(context)
    verify(student.playAGame)(context)
    verify(employee.goToOffice)(context)
  }

  test("should set current step at the start of every simulation step") {
    val context = new Context
    val trackStep = spyLambda((step: Int) => {})
    val agent = new Agent
    agent.addBehaviour((context: Context) =>
      trackStep(context.simulationContext.getCurrentStep())
    )

    context.agents.add(agent);
    val steps = 3
    context.simulationContext.setSteps(steps);

    val order: InOrder = inOrder(trackStep);
    Simulation.run(context)

    order.verify(trackStep)(1)
    order.verify(trackStep)(2)
    order.verify(trackStep)(3)
  }

  test(
    "should execute all Behaviours of all agents for specified number of steps"
  ) {
    val context = new Context
    val student1 = new Student
    val student2 = new Student
    context.agents.add(student1);
    context.agents.add(student2);
    val steps = 2
    context.simulationContext.setSteps(steps);

    val order: InOrder = inOrder(
      student1._goToSchoolForStep,
      student1._playAGameForStep,
      student2._goToSchoolForStep,
      student2._playAGameForStep
    );

    Simulation.run(context)

    order.verify(student1._goToSchoolForStep)(1)
    order.verify(student1._playAGameForStep)(1)
    order.verify(student2._goToSchoolForStep)(1)
    order.verify(student2._playAGameForStep)(1)

    order.verify(student1._goToSchoolForStep)(2)
    order.verify(student1._playAGameForStep)(2)
    order.verify(student2._goToSchoolForStep)(2)
    order.verify(student2._playAGameForStep)(2)
  }
}

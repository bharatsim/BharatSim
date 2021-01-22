package com.bharatsim.engine.testModels

import com.bharatsim.engine.Context
import com.bharatsim.engine.models.Agent
import com.bharatsim.engine.testModels.Employee.employeeBehaviour
import org.mockito.MockitoSugar.spyLambda

case class Employee() extends Agent {
  val goToOffice: Context => Unit = employeeBehaviour
  addBehaviour(goToOffice)
}

object Employee {
  val employeeBehaviour: Context => Unit = spyLambda[Context => Unit]((_: Context) => {})
}
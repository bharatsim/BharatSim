package com.bharatsim.engine.execution.control

import com.bharatsim.engine.Context
import com.bharatsim.engine.graph.neo4j.BatchNeo4jProvider
import com.bharatsim.engine.models.StatefulAgent
import org.neo4j.driver.Record
import scala.jdk.CollectionConverters.MapHasAsJava

import scala.concurrent.ExecutionContext

class DistributeStateControl(context: Context) {
  def executeFor(statefulAgent: StatefulAgent): Unit = {
    val currentState = statefulAgent.activeState
    currentState.perTickAction(context, statefulAgent)

    val maybeTransition = currentState.transitions.find(_.when(context, statefulAgent))
    if (maybeTransition.isDefined) {
      val transition = maybeTransition.get
      val state = transition.state(context)

      val queryParams = new java.util.HashMap[String, java.lang.Object]()
      queryParams.put("stateId", currentState.internalId.asInstanceOf[Object])
      queryParams.put("agentId", statefulAgent.internalId.asInstanceOf[Object])
      queryParams.put("nodeParams", transition.serializedState(state).asJava)
      context.graphProvider
        .asInstanceOf[BatchNeo4jProvider]
        .executeWrite(
          s"""MATCH (s) where id(s) = props.stateId
             |DETACH DELETE s with 0 as something, props
             |MATCH (a) where id(a) = props.agentId
             |CREATE (newState:${transition.label}) SET newState=props.nodeParams
             |CREATE (a)-[:${StatefulAgent.STATE_RELATIONSHIP}]->(newState)
             |RETURN id(newState) as newStateId""".stripMargin,
          queryParams
        )
        .onComplete {
          case x: scala.util.Success[Record] =>
            val nodeId = x.value.get("newStateId").asInt()

            state.setId(nodeId)
            state.enterAction(context, statefulAgent)
        }(ExecutionContext.global)
    }
  }
}

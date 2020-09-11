package com.bharatsim.engine
import com.bharatsim.engine.graph.GraphNode

object Simulation {

  def run(context: Context): Unit = {
    for (step <- 1 to context.simulationContext.simulationSteps) {
      context.simulationContext.setCurrentStep(step)

      val agentTypes = context.fetchAgentTypes

      agentTypes.foreach(agentType => {
        val label = agentType.split('.').last

        context.graphProvider.fetchNodes(label).foreach(graphNode => {
          val modelInstance: Agent = asDomainModel(graphNode, agentType)

          modelInstance.behaviours.foreach(b => b(context))
        })
      })
    }
  }

  private def asDomainModel(graphNode: GraphNode, className: String) = {
    val value = Class.forName(className)
    val modelInstance = value.getDeclaredConstructor().newInstance().asInstanceOf[Agent]
    modelInstance.setId(graphNode.Id)
    modelInstance.setParams(graphNode.getParams)
    modelInstance
  }
}

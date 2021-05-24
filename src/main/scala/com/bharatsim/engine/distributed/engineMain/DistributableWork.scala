package com.bharatsim.engine.distributed.engineMain

class DistributableWork(agentLabels: List[String], val batchSize: Int, val finishedCount: Int = 0) {
  def agentLabel: String = agentLabels.head
  def nextBatch: DistributableWork = {
    new DistributableWork(agentLabels, batchSize, finishedCount + batchSize)
  }

  def nextAgentLabel: DistributableWork = {
    new DistributableWork(agentLabels.tail, batchSize)
  }

  def isComplete: Boolean = agentLabels.isEmpty
}

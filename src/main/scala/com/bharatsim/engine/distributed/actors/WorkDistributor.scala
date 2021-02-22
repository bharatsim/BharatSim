//package com.bharatsim.engine.distributed.actors
//
//import akka.actor.typed.scaladsl.{AbstractBehavior, ActorContext, Behaviors}
//import akka.actor.typed.{ActorRef, Behavior}
//import com.bharatsim.engine.Context
//import com.bharatsim.engine.distributed.WorkerManager
//import com.bharatsim.engine.distributed.WorkerManager.WorkMessage
//import com.bharatsim.engine.distributed.actors.Barrier.SetWorkCount
//import com.bharatsim.engine.distributed.actors.WorkDistributor.{Command, FetchForLabel, FetchForNextLabel}
//import com.bharatsim.engine.execution.actorbased.RoundRobinStrategy
//import com.bharatsim.engine.graph.patternMatcher.EmptyPattern
//
//class WorkDistributor(
//    actorContext: ActorContext[Command],
//    workers: Array[ActorRef[WorkerManager.Command]],
//    barrier: ActorRef[Barrier.Command],
//    simulationContext: Context
//) extends AbstractBehavior[Command](actorContext) {
//  private val roundRobinStrategy = new RoundRobinStrategy(workers.length)
//  private var workCount = 0
//  private val labels = simulationContext.agentLabels.iterator
//  private val batchSize = simulationContext.simulationConfig.countBatchSize
//
//  override def onMessage(msg: Command): Behavior[Command] =
//    msg match {
//      case FetchForNextLabel =>
//        if (labels.hasNext) {
//          actorContext.self ! FetchForLabel(labels.next(), 0, batchSize, 0)
//          Behaviors.same
//        } else {
//          barrier ! SetWorkCount(workCount)
//          Behaviors.stopped
//        }
//      case FetchForLabel(label, skip, limit, total) =>
//        fetchForLabel(label, skip, limit, total)
//        Behaviors.same
//    }
//
//  private def fetchForLabel(label: String, skip: Int, limit: Int, total: Int): Unit = {
//    val graphNodes = simulationContext.graphProvider.fetchNodesSelect(label, Set.empty, EmptyPattern(), skip, limit)
//    val worker = workers(roundRobinStrategy.next)
//
//    worker ! WorkMessage(graphNodes, barrier)
//
//    if (graphNodes.isEmpty) {
//      workCount += total
//      actorContext.self ! FetchForNextLabel
//    } else {
//      val fetchedInThisCycle = graphNodes.size
//      actorContext.self ! FetchForLabel(label, skip + fetchedInThisCycle, limit, total + fetchedInThisCycle)
//    }
//  }
//}
//
//object WorkDistributor {
//  sealed trait Command
//
//  case object FetchForNextLabel extends Command
//  case class FetchForLabel(label: String, skip: Int, limit: Int, total: Int) extends Command
//
//  def apply(
//      workers: Array[ActorRef[WorkerManager.Command]],
//      barrier: ActorRef[Barrier.Command],
//      simulationContext: Context
//  ): Behavior[Command] =
//    Behaviors.setup(context => new WorkDistributor(context, workers, barrier, simulationContext))
//}

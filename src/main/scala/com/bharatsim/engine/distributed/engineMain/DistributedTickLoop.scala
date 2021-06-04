package com.bharatsim.engine.distributed.engineMain

import akka.actor.CoordinatedShutdown
import akka.actor.typed.{ActorRef, Behavior}
import akka.actor.typed.scaladsl.{AbstractBehavior, ActorContext, Behaviors}
import com.bharatsim.engine.Context
import com.bharatsim.engine.distributed.Guardian.UserInitiatedShutdown
import com.bharatsim.engine.distributed.engineMain.Barrier.{BarrierFinished, Reply, WorkErrored, WorkFinished}
import com.bharatsim.engine.distributed.engineMain.DistributedTickLoop._
import com.bharatsim.engine.distributed.{CborSerializable, DBBookmark}
import com.bharatsim.engine.execution.actions.Actions
import com.bharatsim.engine.graph.neo4j.BatchNeo4jProvider
import com.typesafe.scalalogging.LazyLogging

import scala.util.{Failure, Success}

class DistributedTickLoop(
    actorContext: ActorContext[Command],
    simulationContext: Context,
    actions: Actions,
    currentTick: Int,
    bookmarks: List[DBBookmark],
    workerCoordinator: WorkerCoordinator
) extends AbstractBehavior(actorContext)
    with LazyLogging {

  start()

  override def onMessage(msg: Command): Behavior[Command] =
    msg match {
      case ExecuteWrites =>
        executePendingWrites()
        Behaviors.same

      case WriteFinished(bookmarks) =>
        logger.debug("Finished executing pending writes for tick {}", simulationContext.getCurrentStep)
        actions.postTick.execute()
        DistributedTickLoop(simulationContext, actions, currentTick + 1, bookmarks, workerCoordinator)

      case ShutdownSystem(reason, origin) =>
        logger.info("Shutdown initiated by {} : {}", origin.path, reason)
        workerCoordinator.triggerShutdown(reason, origin)
        CoordinatedShutdown(context.system).run(UserInitiatedShutdown)
        Behaviors.stopped
    }

  private def isEndOfSimulation =
    currentTick > simulationContext.simulationConfig.simulationSteps || simulationContext.stopSimulation

  private def start(): Unit = {
    simulationContext.graphProvider.asInstanceOf[BatchNeo4jProvider].setBookmarks(bookmarks)

    if (isEndOfSimulation) {
      actions.postSimulation.execute()
      context.self ! ShutdownSystem(SIMULATION_FINISHED, context.self)
      return
    }
    actions.preTick.execute(currentTick)
    workerCoordinator.initTick(context.system, simulationContext, bookmarks)
    val distributableWork =
      new DistributableWork(simulationContext.agentLabels.toList, simulationContext.simulationConfig.workBatchSize)
    if (distributableWork.isComplete) {
      context.self ! DistributedTickLoop.ExecuteWrites
      return
    }
    val adaptedReply = actorContext.messageAdapter[Reply] {
      case BarrierFinished(_)                    => ExecuteWrites
      case Barrier.BarrierAborted(error, origin) => ShutdownSystem(error, origin)
    }
    val barrier =
      context.spawn(Barrier(0, workerCoordinator.workerCount, adaptedReply), s"${WORK_BARRIER}-${currentTick}")
    val workDistributor =
      context.spawn(WorkDistributor(barrier, distributableWork), s"${WORK_DISTRIBUTOR}-${currentTick}")

    workerCoordinator.startWork(workDistributor)

  }

  private def executePendingWrites(): Unit = {
    logger.debug("Started executing pending writes for tick {}", simulationContext.getCurrentStep)
    val eventualLocalBookmark = simulationContext.graphProvider
      .asInstanceOf[BatchNeo4jProvider]
      .executePendingWrites()

    val adaptedReply = actorContext.messageAdapter[Reply] {
      case BarrierFinished(bookmarks)            => WriteFinished(bookmarks)
      case Barrier.BarrierAborted(error, origin) => ShutdownSystem(error, origin)
    }

    val barrier =
      context.spawn(Barrier(0, workerCoordinator.workerCount + 1, adaptedReply), s"${WRITE_BARRIER}-${currentTick}")

    workerCoordinator.executeWrites(barrier)

    eventualLocalBookmark.onComplete({
      case Success(bookmark: DBBookmark) => {
        barrier ! WorkFinished(Some(bookmark))
      }
      case Failure(exception) =>
        logger.error(
          "ExecutePendingWrites Failed: {} \n {}",
          exception.toString,
          exception.getStackTrace.mkString("\n")
        )
        context.self ! ShutdownSystem(s"ExecutePendingWrites Failed ${exception.toString}", context.self)
    })(context.executionContext)
  }

}

object DistributedTickLoop {
  def apply(
      simContext: Context,
      actions: Actions,
      currentTick: Int,
      bookmarks: List[DBBookmark] = List.empty,
      workerCoordinator: WorkerCoordinator = new WorkerCoordinator()
  ): Behavior[Command] = {
    Behaviors.setup(context => {
      actions.preSimulation.execute()
      new DistributedTickLoop(context, simContext, actions, currentTick, bookmarks, workerCoordinator)
    })
  }
  val WORK_BARRIER = "work-barrier"
  val WORK_DISTRIBUTOR = "work-distributor"
  val WRITE_BARRIER = "write-barrier"
  val SIMULATION_FINISHED = "Simulation Finished"

  sealed trait Command extends CborSerializable
  case object ExecuteWrites extends Command
  case class WriteFinished(bookmarks: List[DBBookmark]) extends Command
  case class StartOfNewTickAck() extends CborSerializable
  case class ShutdownSystem(reason: String, origin: ActorRef[_]) extends Command
}

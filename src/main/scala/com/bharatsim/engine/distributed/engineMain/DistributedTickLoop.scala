package com.bharatsim.engine.distributed.engineMain

import akka.actor.CoordinatedShutdown
import akka.actor.typed.Behavior
import akka.actor.typed.scaladsl.{AbstractBehavior, ActorContext, Behaviors}
import com.bharatsim.engine.Context
import com.bharatsim.engine.distributed.Guardian.UserInitiatedShutdown
import com.bharatsim.engine.distributed.engineMain.Barrier.{BarrierFinished, WorkFinished}
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

  star()

  override def onMessage(msg: Command): Behavior[Command] =
    msg match {
      case ExecuteWrites =>
        executePendingWrites()
        Behaviors.same

      case WriteFinished(bookmarks) =>
        context.log.info("Finished executing pending writes for tick {}", simulationContext.getCurrentStep)
        actions.postTick.execute()
        DistributedTickLoop(simulationContext, actions, currentTick + 1, bookmarks, workerCoordinator)
    }

  private def isEndOfSimulation =
    currentTick > simulationContext.simulationConfig.simulationSteps || simulationContext.stopSimulation

  private def star(): Unit = {
    simulationContext.graphProvider.asInstanceOf[BatchNeo4jProvider].setBookmarks(bookmarks)

    if (isEndOfSimulation) {
      actions.postSimulation.execute()
      CoordinatedShutdown(context.system).run(UserInitiatedShutdown)
      return
    }
    actions.preTick.execute(currentTick)
    workerCoordinator.initTick(context.system, simulationContext, bookmarks)
    val distributableWork =
      new DistributableWork(simulationContext.agentLabels.toList, simulationContext.simulationConfig.countBatchSize)
    if (distributableWork.isComplete) {
      context.self ! DistributedTickLoop.ExecuteWrites
      return
    }
    val adaptedReply = actorContext.messageAdapter[BarrierFinished](_ => ExecuteWrites)
    val barrier =
      context.spawn(Barrier(0, workerCoordinator.workerCount, adaptedReply), s"${WORK_BARRIER}-${currentTick}")
    val workDistributor =
      context.spawn(WorkDistributor(barrier, distributableWork), s"${WORK_DISTRIBUTOR}-${currentTick}")

    workerCoordinator.startWork(workDistributor)

  }

  private def executePendingWrites(): Unit = {
    context.log.info("Started executing pending writes for tick {}", simulationContext.getCurrentStep)
    val eventualLocalBookmark = simulationContext.graphProvider
      .asInstanceOf[BatchNeo4jProvider]
      .executePendingWrites()

    val adaptedReply =
      actorContext.messageAdapter[BarrierFinished](response => WriteFinished(response.bookmarks))
    val barrier =
      context.spawn(Barrier(0, workerCoordinator.workerCount + 1, adaptedReply), s"${WRITE_BARRIER}-${currentTick}")

    workerCoordinator.executeWrites(barrier)

    eventualLocalBookmark.onComplete({
      case Success(bookmark: DBBookmark) => {
        barrier ! WorkFinished(Some(bookmark))
      }
      case Failure(exception) =>
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

  sealed trait Command extends CborSerializable
  case object ExecuteWrites extends Command
  case class WriteFinished(bookmarks: List[DBBookmark]) extends Command
  case class ContextUpdateDone() extends CborSerializable
}

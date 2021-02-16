package com.bharatsim.engine.distributed

import akka.actor.typed.Behavior
import akka.actor.typed.scaladsl.Behaviors
import akka.cluster.ddata.{LWWMap, LWWMapKey, SelfUniqueAddress}
import akka.cluster.ddata.typed.scaladsl.DistributedData
import akka.cluster.ddata.typed.scaladsl.Replicator._
import com.bharatsim.engine.Context
object SimulationContextReplicator {

  val contextKey = "ReplicatedSimulationContext"
  case class ContextData(currentTick: Int, activeIntervention: Set[String]) extends CborSerializable

  private def fromContext(context: Context): ContextData = {
    ContextData(context.getCurrentStep, context.activeInterventionNames)
  }
  sealed trait Command extends CborSerializable
  final case class UpdateContext() extends Command
  private sealed trait InternalCommand extends Command
  private case class InternalUpdateResponse(rsp: UpdateResponse[LWWMap[String, ContextData]]) extends InternalCommand

  def apply(simulationContext: Context): Behavior[Command] =
    Behaviors.setup { actorContext =>
      DistributedData.withReplicatorMessageAdapter[Command, LWWMap[String, ContextData]] { replicator =>
        implicit val node: SelfUniqueAddress = DistributedData(actorContext.system).selfUniqueAddress

        def dataKey: LWWMapKey[String, ContextData] = LWWMapKey(contextKey)

        Behaviors.receiveMessage[Command] {
          case UpdateContext() =>
            replicator.askUpdate(
              askReplyTo =>
                Update(dataKey, LWWMap.empty[String, ContextData], WriteLocal, askReplyTo)(
                  _ :+ (contextKey -> fromContext(simulationContext))
                ),
              InternalUpdateResponse.apply
            )
            Behaviors.same
          case InternalUpdateResponse(_) => Behaviors.same
        }
      }
    }

}

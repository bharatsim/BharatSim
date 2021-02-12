package com.bharatsim.engine.distributed
import akka.actor.typed.Behavior
import akka.actor.typed.scaladsl.Behaviors
import akka.cluster.ddata.{LWWMap, LWWMapKey, SelfUniqueAddress}
import akka.cluster.ddata.typed.scaladsl.{DistributedData, Replicator}
import com.bharatsim.engine.Context
import com.bharatsim.engine.distributed.SimulationContextReplicator.{ContextData, contextKey}

object SimulationContextSubscriber {

  sealed trait InternalCommand

  private case class InternalSubscribeResponse(chg: Replicator.SubscribeResponse[LWWMap[String, ContextData]])
      extends InternalCommand

  def apply(simulationContext: Context): Behavior[InternalCommand] =
    Behaviors.setup { context =>
      DistributedData.withReplicatorMessageAdapter[InternalCommand, LWWMap[String, ContextData]] { replicator =>
        implicit val node: SelfUniqueAddress = DistributedData(context.system).selfUniqueAddress
        def dataKey: LWWMapKey[String, ContextData] = LWWMapKey(contextKey)
        replicator.subscribe(dataKey, InternalSubscribeResponse.apply)

        Behaviors.receiveMessage[InternalCommand] {

          case InternalSubscribeResponse(chg @ Replicator.Changed(dataKey)) =>
            val contextData: ContextData = chg.get(dataKey).get(contextKey).get
            simulationContext.setCurrentStep(contextData.currentTick)
            simulationContext.setActiveIntervention(contextData.activeIntervention)
            Behaviors.same

        }
      }
    }

}

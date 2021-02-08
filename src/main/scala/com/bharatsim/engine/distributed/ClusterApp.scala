package com.bharatsim.engine.distributed

import akka.actor.typed.ActorSystem
import com.typesafe.config.ConfigFactory

object ClusterApp {
  def main(args: Array[String]): Unit = {
    startStore(25251)
    startClient(25252)
  }

  private def startStore(port: Int): Unit = {
    val config = ConfigFactory
      .parseString(s"""
      akka.remote.artery.canonical.port=$port
      """)
      .withFallback(ConfigFactory.load("cluster"))
    ActorSystem[Nothing](Store.setup(), "Cluster", config)
  }

  private def startClient(port: Int): Unit = {
    val config = ConfigFactory
      .parseString(s"""
      akka.remote.artery.canonical.port=$port
      """)
      .withFallback(ConfigFactory.load("cluster"))
    ActorSystem[Nothing](StoreClient.setup(), "Cluster", config)
  }
}

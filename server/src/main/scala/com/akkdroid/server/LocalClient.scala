package com.akkdroid.server

import akka.actor.ActorSystem
import com.typesafe.config.{ConfigValueFactory, ConfigFactory, Config}
import akka.event.Logging

object LocalClient {
  def main(args: Array[String]) {
    val config = ConfigFactory.load("application.conf")
      .withValue("akka.remote.netty.port", ConfigValueFactory.fromAnyRef(2553))

    val system = ActorSystem("local-system", config)
    system.eventStream.setLogLevel(Logging.DebugLevel)

    val actor = system.actorFor("akka://server-system@192.168.192.245:2552/user/server-actor")

    actor ! "do something"

    system.shutdown()
  }
}

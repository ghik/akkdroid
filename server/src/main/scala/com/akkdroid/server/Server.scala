package com.akkdroid.server

import akka.actor.{Props, ActorSystem}
import akka.event.Logging
import com.typesafe.config.{ConfigValueFactory, ConfigFactory}

/**
 * Created with IntelliJ IDEA.
 * User: ghik
 * Date: 28.04.13
 * Time: 13:11
 */
object Server {
  def main(args: Array[String]) {
    var config = ConfigFactory.load()
    if (args.length > 0) {
      config = config.withValue("akka.remote.netty.tcp.hostname", ConfigValueFactory.fromAnyRef(args(0)))
    }
    val system = ActorSystem("server-system", config)
    system.eventStream.setLogLevel(Logging.DebugLevel)

    val actor = system.actorOf(Props[ServerActor], "server-actor")
  }
}

package com.akkdroid.server

import akka.actor.{Props, ActorSystem}
import akka.event.Logging

/**
 * Created with IntelliJ IDEA.
 * User: ghik
 * Date: 28.04.13
 * Time: 13:11
 */
object Server {
  def main(args: Array[String]) {
    val system = ActorSystem("server-system")
    system.eventStream.setLogLevel(Logging.DebugLevel)

    val actor = system.actorOf(Props[LoggingActor], "server-actor")
  }
}

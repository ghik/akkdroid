package com.akkdroid.server

import akka.actor.Actor

class LoggingActor extends Actor {
  def receive = {
    case msg => println(s"$sender says: $msg")
  }
}

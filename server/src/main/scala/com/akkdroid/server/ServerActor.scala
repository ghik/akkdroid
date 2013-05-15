package com.akkdroid.server

import akka.actor.Actor

class ServerActor extends Actor {
  def receive = {
    case msg =>
      println(s"$sender says: $msg")
      sender ! s"You said: $msg"
  }
}

package com.akkdroid.client

import akka.actor.Actor
import android.util.Log

class LoggingActor extends Actor {
  def receive = {
    case msg =>
      Log.i("Akka message", s"$sender says: $msg")
  }
}

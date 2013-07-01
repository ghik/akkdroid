package com.akkdroid.client

import akka.actor.{ActorRef, Actor}
import com.akkdroid.client.TalkActor.{ForwardMessage, TalkMessage}
import android.util.Log

class TalkActor extends Actor {
  def receive = {
    case TalkMessage(sender, text) => Log.i("TalkActor", s"$sender:$text")
    case ForwardMessage(to, sender, text) => to ! TalkMessage(sender, text)
  }
}

object TalkActor {
  case class TalkMessage(sender: String, text: String)
  case class ForwardMessage(to: ActorRef, sender: String, text: String)
}

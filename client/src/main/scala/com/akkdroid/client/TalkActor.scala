package com.akkdroid.client

import akka.actor.{ActorSelection, ActorRef, Actor}
import com.akkdroid.client.TalkActor.{SetTalkListener, ForwardMessage, TalkMessage}
import android.util.Log

class TalkActor extends Actor {
  private var listener: ActorRef = null
  def receive = {
    case TalkMessage(sender, text) => {
      Log.i("TalkActor", s"$sender:$text")
      if (listener != null)
        listener ! TalkMessage(sender, text)
    }
    case ForwardMessage(to, sender, text) => {
      Log.w("TalkAkktivity", "sending message")
      to ! TalkMessage(sender, text)
    }
    case SetTalkListener(actor) => {
      Log.e("TalkAkktivity", "setlistener")
      listener = actor
    }

  }
}

object TalkActor {
  case class TalkMessage(sender: String, text: String)
  case class SetTalkListener(listener: ActorRef)
  case class ForwardMessage(to: ActorSelection, sender: String, text: String)
}

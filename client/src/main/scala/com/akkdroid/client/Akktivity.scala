package com.akkdroid.client

import android.app.Activity
import android.os.Bundle
import akka.actor.{ActorRef, ActorSystem, Props}
import android.widget.{TextView, Button}

class Akktivity extends Activity {

  import Conversions._

  private var system: ActorSystem = null
  private var actor: ActorRef = null
  private var serverActor: ActorRef = null

  private var messageTextView: TextView = null
  private var sendButton: Button = null

  override def onCreate(savedInstanceState: Bundle) {
    super.onCreate(savedInstanceState)

    if (system == null) {
      system = ActorSystem("mobile-system")
      actor = system.actorOf(Props[LoggingActor], name = "mobile-actor")
      serverActor = system.actorFor("akka://server-system@192.168.192.245:2552/user/server-actor")
    }

    setContentView(R.layout.main)

    sendButton = findViewById(R.id.sendButton).asInstanceOf[Button]
    messageTextView = findViewById(R.id.messageText).asInstanceOf[TextView]

    sendButton.onClick { _ =>
      serverActor ! messageTextView.getText.toString
    }
  }

  override def onDestroy() {
    system.shutdown()

    super.onDestroy()
  }
}

package com.akkdroid.client

import android.app.Activity
import android.os.{Handler, Bundle}
import akka.actor.{ActorRef, ActorSystem, Props}
import android.widget.{ArrayAdapter, ListView, TextView, Button}
import java.{lang => jl, util => ju}

class Akktivity extends Activity {

  import Conversions._

  private var system: ActorSystem = null
  private var localActor: ActorRef = null
  private var serverActor: ActorRef = null

  private var messageTextView: TextView = null
  private var sendButton: Button = null
  private var messagesList: ListView = null

  override def onCreate(savedInstanceState: Bundle) {
    super.onCreate(savedInstanceState)

    setContentView(R.layout.main)

    sendButton = findViewById(R.id.sendButton).asInstanceOf[Button]
    messageTextView = findViewById(R.id.messageText).asInstanceOf[TextView]
    messagesList = findViewById(R.id.messagesList).asInstanceOf[ListView]

    // ju means java.util (see imports)
    val items = new ju.ArrayList[String]
    val adapter = new ArrayAdapter(this, android.R.layout.simple_list_item_1, items)
    messagesList.setAdapter(adapter)

    if (system == null) {
      system = ActorSystem("mobile-system")

      // all messages received by local actor will be passed to handler and handled with code below
      val handler = new Handler
      def newLocalActor = new HandlerDispatcherActor(handler, msg => {
        items.add(msg.toString)
        adapter.notifyDataSetChanged()
      })

      localActor = system.actorOf(Props(newLocalActor), name = "mobile-actor")
      serverActor = system.actorFor("akka://server-system@192.168.192.245:2552/user/server-actor")
    }

    sendButton.onClick { _ =>
      implicit val sender = localActor // impersonate our local actor so it can receive responses from server
      serverActor ! messageTextView.getText.toString
    }
  }

  override def onDestroy() {
    system.shutdown()

    super.onDestroy()
  }
}

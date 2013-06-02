package com.akkdroid.client

import android.app.Activity
import android.os.{Handler, Bundle}
import akka.actor.{Props, ActorRef, ActorSystem}
import android.widget._
import java.{lang => jl, util => ju}
import android.content.Intent
import android.preference.PreferenceManager

class Akktivity extends Activity {

  import Conversions._

  private var system: ActorSystem = null
  private var serviceURL: String = null
  private var adapter: ArrayAdapter[String] = null
  private var localActor: ActorRef = null
  private var serverActor: ActorRef = null

  private var messageTextView: TextView = null
  private var sendButton: Button = null
  private var settingsButton: Button = null
  private var messagesList: ListView = null

  override def onCreate(savedInstanceState: Bundle) {
    super.onCreate(savedInstanceState)

    setContentView(R.layout.main)
    loadUI

    val items = new ju.ArrayList[String]
    adapter = new ArrayAdapter(this, android.R.layout.simple_list_item_1, items)
    messagesList.setAdapter(adapter)

    initializeActors

    sendButton.onClickAsync { _ =>
      implicit val sender = localActor // impersonate our local actor so it can receive responses from server
      serverActor ! messageTextView.getText.toString
    }
    settingsButton.onClick { _ =>
      startActivity(new Intent(getBaseContext(), classOf[AkkdroidPreferences]))
    }
  }

  override def onStart {
    super.onStart
    initializeActors
  }

  override def onResume {
    super.onResume
    initializeActors
  }

  override def onDestroy {
    system.shutdown

    super.onDestroy
  }

  private def loadUI {
    sendButton = findViewById(R.id.sendButton).asInstanceOf[Button]
    messageTextView = findViewById(R.id.messageText).asInstanceOf[TextView]
    settingsButton = findViewById(R.id.settingsButton).asInstanceOf[Button]
    messagesList = findViewById(R.id.messagesList).asInstanceOf[ListView]
  }

  private def loadServiceURL() : String = {
    val pref = PreferenceManager.getDefaultSharedPreferences(getBaseContext())
    val ip:String = pref.getString("pref_ip", getString(R.string.pref_ip_value))
    val port:String = pref.getString("pref_port", getString(R.string.pref_port_value))
    return s"akka://server-system@$ip:$port/user/server-actor"
  }

  private def initializeActors {
    if (system == null) {
      system = ActorSystem("mobile-system")

      // all messages received by local actor will be passed to handler and handled with code below
      val handler = new Handler
      def newLocalActor = new HandlerDispatcherActor(handler, msg => {
        adapter.add(msg.toString)
        adapter.notifyDataSetChanged()
      })
      localActor = system.actorOf(Props(newLocalActor), name = "mobile-actor")
    }

    val newServiceURL = loadServiceURL()
    if (newServiceURL != serviceURL) {
      serviceURL = newServiceURL
      serverActor = system.actorFor(serviceURL)
    }
  }
}

package com.akkdroid.client

import android.app.Activity
import android.os.{Handler, Bundle}
import akka.actor.{ActorRef, ActorSystem, Props}
import android.widget._
import java.{lang => jl, util => ju}
import android.content.{DialogInterface, Context, Intent}
import android.view.View.OnClickListener
import android.view.View
import android.preference.PreferenceManager

class Akktivity extends Activity {

  import Conversions._

  private var system: ActorSystem = null
  private var localActor: ActorRef = null
  private var serverActor: ActorRef = null

  private var messageTextView: TextView = null
  private var sendButton: Button = null
  private var settingsButton: Button = null
  private var messagesList: ListView = null

  override def onCreate(savedInstanceState: Bundle) {
    super.onCreate(savedInstanceState)

    setContentView(R.layout.main)

    sendButton = findViewById(R.id.sendButton).asInstanceOf[Button]
    messageTextView = findViewById(R.id.messageText).asInstanceOf[TextView]
    settingsButton = findViewById(R.id.settingsButton).asInstanceOf[Button]
    messagesList = findViewById(R.id.messagesList).asInstanceOf[ListView]

    // ju means java.util (see imports)
    val items = new ju.ArrayList[String]
    val adapter = new ArrayAdapter(this, android.R.layout.simple_list_item_1, items)
    messagesList.setAdapter(adapter)

    Toast.makeText(getApplicationContext(), "hello", Toast.LENGTH_LONG)
    var serviceUrl = null
    if (system == null) {
      system = ActorSystem("mobile-system")

      // all messages received by local actor will be passed to handler and handled with code below
      val handler = new Handler
      def newLocalActor = new HandlerDispatcherActor(handler, msg => {
        items.add(msg.toString)
        adapter.notifyDataSetChanged()
      })
      val pref = PreferenceManager.getDefaultSharedPreferences(getBaseContext())
      val ip:String = pref.getString("pref_ip", getString(R.string.pref_ip_value))
      val port:String = pref.getString("pref_port", getString(R.string.pref_port_value))
      localActor = system.actorOf(Props(newLocalActor), name = "mobile-actor")
      serviceUrl = s"akka://server-system@$ip:$port/user/server-actor"
      Toast.makeText(getApplicationContext(), serviceUrl, Toast.LENGTH_LONG).show()
      serverActor = system.actorFor(serviceUrl)
    }

    sendButton.onClick { _ =>
      implicit val sender = localActor // impersonate our local actor so it can receive responses from server
      serverActor ! messageTextView.getText.toString
    }
    val ac = getApplicationContext()
    settingsButton.setOnClickListener(new OnClickListener {
      def onClick(p1: View) {
        startActivity(new Intent(getBaseContext(), classOf[AkkdroidPreferences]))
        Toast.makeText(ac, serviceUrl, Toast.LENGTH_LONG).show()
      }
    })
  }

  override def onDestroy() {
    system.shutdown()

    super.onDestroy()
  }
}

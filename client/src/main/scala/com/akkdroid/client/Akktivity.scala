package com.akkdroid.client

import android.app.Activity
import android.os.{Handler, Bundle}
import akka.actor.{ActorSelection, Props, ActorRef, ActorSystem}
import android.widget._
import java.{lang => jl, util => ju}
import android.content.Intent
import android.preference.PreferenceManager
import java.net.{InetAddress, NetworkInterface}
import scala.collection.JavaConverters._
import com.typesafe.config.{ConfigValueFactory, ConfigFactory}
import com.akkdroid.util.EnumerationIterator
import scala.concurrent.duration._
import scala.concurrent.{Promise, Await}
import com.akkdroid.client.MembersManager.{SetListener, GetMembers}
import android.util.Log

class Akktivity extends Activity {

  import Conversions._

  private val config = ConfigFactory.load()
    .withValue("akka.remote.netty.tcp.hostname", ConfigValueFactory.fromAnyRef(
    getInetAddress.getOrElse(throw new Exception("No public IP address found!"))))

  private var system: ActorSystem = null
  private var serviceURL: String = null
  private var adapter: ArrayAdapter[String] = null
  private var localActor: ActorRef = null
  private var serverActor: ActorSelection = null
  private var membersManager: ActorRef = null

  private var messageTextView: TextView = null
  private var sendButton: Button = null
  private var settingsButton: Button = null
  private var messagesList: ListView = null

  // obtains list of addresses of members that have recently sent us ping message
  // this is done by consulting membersManager actor
  def getView: List[InetAddress] = {
    val p = Promise[List[InetAddress]]
    membersManager ! GetMembers(p)
    Await.result(p.future, Duration.Inf)
  }

  override def onCreate(savedInstanceState: Bundle) {
    super.onCreate(savedInstanceState)


    val items = new ju.ArrayList[String]
    adapter = new ArrayAdapter(this, android.R.layout.simple_list_item_1, items)
    initActorSystem()
    updateServerActorRef()

    loadContactsUI()
  }

  private def initActorSystem() {
    system = ActorSystem("mobile-system", config)
    membersManager = system.actorOf(Props(new MembersManager(config)), name = "membersManager")

    val tickInterval = config.getInt("akkdroid.view.update-interval")
    implicit val executionContext = system.dispatcher
    system.scheduler.schedule(0.seconds, tickInterval.seconds, new PingSender(config, membersManager))
    new PingReceiver(config, membersManager).start()

    // all messages received by local actor will be passed to handler and handled with code below
    val handler = new Handler
    def newLocalActor = new HandlerDispatcherActor(handler, msg => {
      adapter.add(msg.toString)
      adapter.notifyDataSetChanged()
    })
    localActor = system.actorOf(Props(newLocalActor), name = "mobile-actor")
  }

  override def onStart() {
    super.onStart()
    updateServerActorRef()
  }

  override def onResume() {
    super.onResume()
    updateServerActorRef()
  }

  override def onDestroy() {
    system.shutdown()

    super.onDestroy()
  }

  private def loadContactsUI() {
    setContentView(R.layout.contacts)
    val contactsList = findViewById(R.id.peerList).asInstanceOf[ListView]

    val items = new ju.ArrayList[String]
    val adapter = new ArrayAdapter(this, android.R.layout.simple_list_item_1, items)
    contactsList.setAdapter(adapter)
    //contactsList.onClick()
    val handler = new Handler
    def newLocalActor = new HandlerDispatcherActor(handler, msg => {
      adapter.clear()
      val list = getView
      list.foreach(a => adapter.add(a.toString))
      Log.i("Akktivity", "updated peer list")
    })
    val localActor = system.actorOf(Props(newLocalActor), name = "peers-update-handler")
    membersManager ! SetListener(localActor)

  }

  private def loadTalkUI() {
    setContentView(R.layout.talk)
    sendButton = findViewById(R.id.sendButton).asInstanceOf[Button]
    messageTextView = findViewById(R.id.messageText).asInstanceOf[TextView]
    settingsButton = findViewById(R.id.settingsButton).asInstanceOf[Button]
    messagesList = findViewById(R.id.messagesList).asInstanceOf[ListView]
    messagesList.setAdapter(adapter)

    sendButton.onClickAsync { _ =>
      implicit val sender = localActor // impersonate our local actor so it can receive responses from server
      serverActor ! messageTextView.getText.toString
    }
    settingsButton.onClick { _ =>
      startActivity(new Intent(getBaseContext, classOf[AkkdroidPreferences]))
    }
  }

  private def loadServiceURL(): String = {
    val pref = PreferenceManager.getDefaultSharedPreferences(getBaseContext)
    val ip: String = pref.getString("pref_ip", getString(R.string.pref_ip_value))
    val port: String = pref.getString("pref_port", getString(R.string.pref_port_value))
    s"akka.tcp://server-system@$ip:$port/user/server-actor"
  }

  private def getInetAddress =
    new EnumerationIterator(NetworkInterface.getNetworkInterfaces).asScala.collectFirst {
      case iface if iface.isUp && !iface.isLoopback && iface.getInetAddresses.hasMoreElements =>
        iface.getInetAddresses.nextElement().getHostAddress
    }

  private def updateServerActorRef() {
    val newServiceURL = loadServiceURL()
    if (newServiceURL != serviceURL) {
      serviceURL = newServiceURL
      serverActor = system.actorSelection(serviceURL)
    }
  }
}

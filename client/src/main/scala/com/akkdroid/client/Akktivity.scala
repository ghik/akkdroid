package com.akkdroid.client

import android.app.Activity
import android.os.{Handler, Bundle}
import akka.actor.{ActorSelection, Props, ActorRef, ActorSystem}
import android.widget._
import java.{lang => jl, util => ju}
import android.content.Intent
import android.preference.PreferenceManager
import java.net.{Inet4Address, InetAddress, NetworkInterface}
import scala.collection.JavaConverters._
import com.typesafe.config.{Config, ConfigValueFactory, ConfigFactory}
import com.akkdroid.util.EnumerationIterator
import scala.concurrent.duration._
import scala.concurrent.{Promise, Await}
import com.akkdroid.client.MembersManager.{SetMemberUpdateListener, GetMembers}
import android.util.Log
import android.view.{View, MenuItem, Menu}
import java.util.concurrent.atomic.AtomicReference

class Akktivity extends Activity {

  import Conversions._


  private val config = new AtomicReference[Config](ConfigFactory.load())

  private var system: ActorSystem = null
  private var serviceURL: String = null
  private var adapter: ArrayAdapter[String] = null
  private var localActor: ActorRef = null
  private var talkActor: ActorRef = null
  private var serverActor: ActorSelection = null
  private var membersManager: ActorRef = null

  private var settingsMenu: MenuItem = null


  override def onCreateOptionsMenu(menu: Menu) : Boolean = {
    settingsMenu = menu.add(Menu.NONE, 0, 0, "Show current settings")
    super.onCreateOptionsMenu(menu)
  }

  override def onOptionsItemSelected(menu : MenuItem) : Boolean = {
    if (menu equals settingsMenu) {
        startActivity(new Intent(getBaseContext, classOf[AkkdroidPreferences]))
        loadSettings()
        true
    } else false
  }

  override def onCreate(savedInstanceState: Bundle) {
    super.onCreate(savedInstanceState)
    loadSettings()
    val items = new ju.ArrayList[String]
    adapter = new ArrayAdapter(this, android.R.layout.simple_expandable_list_item_2, items)
    initActorSystem()
    updateServerActorRef()

    loadContactsUI()
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

  // obtains list of addresses of members that have recently sent us ping message
  // this is done by consulting membersManager actor
  private def getView: List[Peer] = {
    val p = Promise[List[Peer]]
    membersManager ! GetMembers(p)
    Await.result(p.future, Duration.Inf)
  }

  private def initActorSystem() {
    system = ActorSystem("mobile-system", config.get())
    Akktivity.system = system
    membersManager = system.actorOf(Props(new MembersManager(config)), name = "membersManager")
    talkActor = system.actorOf(Props(new TalkActor), name = "talk-actor")
    Akktivity.talkActor = talkActor

    val tickInterval = config.get().getInt("akkdroid.view.update-interval")
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

  private def loadContactsUI() {
    setContentView(R.layout.contacts)
    val contactsList = findViewById(R.id.peerList).asInstanceOf[ListView]

    val items = new ju.ArrayList[String]
    val adapter = new ArrayAdapter(this, android.R.layout.simple_list_item_1, items)
    contactsList.setAdapter(adapter)
    val handler = new Handler
    def newLocalActor = new HandlerDispatcherActor(handler, msg => {
      adapter.clear()
      val list = getView
      list.foreach(a => adapter.add(a.nick + " (" + a.addr + ")"))
      Log.i("Akktivity", "updated peer list")
      adapter.notifyDataSetChanged()
    })
    val localActor = system.actorOf(Props(newLocalActor), name = "peers-update-handler")
    membersManager ! SetMemberUpdateListener(localActor)

    class OnClickHandler extends AdapterView.OnItemClickListener {
      override def  onItemClick(parent: AdapterView[_], v: View, position: Int, id: Long) {
        val bundle = new Bundle()
        val peer = getView(position)
        val ip = peer.addr.getHostAddress
        val port = config.get().getInt("akka.remote.netty.tcp.port")
        val remoteRef = s"akka.tcp://mobile-system@$ip:$port/user/talk-actor"
        bundle.putString("remote-nick", peer.nick)
        bundle.putString("remote-ref", remoteRef)
        //bundle.putSerializable("remote-ref", system.actorSelection())
        //bundle.putSerializable("local-ref", talkActor)
        val intent = new Intent(getBaseContext, classOf[TalkAkktivity])
        intent.putExtras(bundle)
        startActivity(intent)
        Log.i("Akktivity", s"pos:$position, id:$id")
      }
    }
    contactsList.setOnItemClickListener(new OnClickHandler)

  }

  private def loadServiceURL(): String = {
    val pref = PreferenceManager.getDefaultSharedPreferences(getBaseContext)
    val ip: String = pref.getString("pref_ip", getString(R.string.pref_ip_value))
    val port: String = pref.getString("pref_port", getString(R.string.pref_port_value))
    s"akka.tcp://server-system@$ip:$port/user/server-actor"
  }

  private def loadSettings() {
    val pref = PreferenceManager.getDefaultSharedPreferences(getBaseContext)
    val newNick: String = pref.getString("pref_nick", getString(R.string.pref_nick_value))
    val hostname = getInetAddress.getOrElse(throw new Exception("No public IP address found!"))
    var conf = config.get()
    conf = conf.withValue("akka.remote.netty.tcp.hostname", ConfigValueFactory.fromAnyRef(hostname))
    conf = conf.withValue("akkdroid.user.nick", ConfigValueFactory.fromAnyRef(newNick))
    config.set(conf)
  }

  private def getInetAddress = {
    var ifaces = new EnumerationIterator(NetworkInterface.getNetworkInterfaces).asScala
    ifaces = ifaces.filter(iface => {iface.isUp && !iface.isLoopback && iface.getInetAddresses.hasMoreElements})
    var addrs = new ju.ArrayList[InetAddress].asScala
    ifaces.foreach( i => i.getInetAddresses.asScala.foreach( a => addrs.append(a)))

    //addrs = addrs.foldLeft(new ju.ArrayList[InetAddress]) ( (l, a) => {l.asScala.appendAll(a.getInetAddresses.asScala); l})

    addrs = addrs.filter(a => a.isInstanceOf[Inet4Address])
    val textAddrs = addrs.map(a => a.getHostAddress)

    Log.e("inet", addrs.toString)
    textAddrs.headOption
    /*NetworkInterface.getNetworkInterfaces.asScala.foreach(n => {Log.e("inet", n.toString); n.getInetAddresses.asScala.foreach(a => Log.e("inet", a.toString + "|" + a.getHostAddress + "|" + a.getClass) )})
    new EnumerationIterator(NetworkInterface.getNetworkInterfaces).asScala.collectFirst {
      case iface if iface.isUp && !iface.isLoopback && iface.getInetAddresses.hasMoreElements
        && iface.getInetAddresses.asScala.exists( a => a.isInstanceOf[Inet4Address]) =>
        iface.getInetAddresses.asScala.collectFirst {
          case addr if addr.isInstanceOf[Inet4Address] => addr.getHostAddress
        }
    } */
  }
  private def updateServerActorRef() {
    val newServiceURL = loadServiceURL()
    if (newServiceURL != serviceURL) {
      serviceURL = newServiceURL
      serverActor = system.actorSelection(serviceURL)
    }
  }
}

object Akktivity {
  var system : ActorSystem = null
  var talkActor : ActorRef = null
}
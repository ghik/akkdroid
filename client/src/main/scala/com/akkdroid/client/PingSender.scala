package com.akkdroid.client

import com.typesafe.config.Config
import java.net.{InetSocketAddress, DatagramPacket, DatagramSocket}
import akka.actor.ActorRef
import com.akkdroid.client.MembersManager.PingSendFailed
import java.util.concurrent.atomic.AtomicReference

class PingSender(val configRef: AtomicReference[Config], listener: ActorRef) extends Runnable with PingConfig {
  val config = configRef.get()
  private val socket = new DatagramSocket

  def run() {
    try {
      val ipbytes = Peer.serialize(Peer(ip, configRef.get().getString("akkdroid.user.nick")))
      val packet = new DatagramPacket(ipbytes, ipbytes.length, new InetSocketAddress(group, port))
      socket.send(packet)
    } catch {
      case e: Exception =>
        listener ! PingSendFailed(e)
    }
  }
}

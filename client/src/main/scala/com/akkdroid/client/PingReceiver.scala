package com.akkdroid.client

import com.typesafe.config.Config
import java.net.{InetAddress, DatagramPacket, MulticastSocket}
import akka.actor.ActorRef
import android.util.Log
import com.akkdroid.client.MembersManager.{PingReceived, PingReceiveFailed}
import java.util.concurrent.atomic.AtomicReference

class PingReceiver(val configRef: AtomicReference[Config], listener: ActorRef) extends Thread with PingConfig {
  val config = configRef.get()
  private val socket = new MulticastSocket(port)
  socket.joinGroup(group)


  override def run() {
    try {
      while (!socket.isClosed) {
        try {
          val packet = Peer.receive(socket)
          listener ! PingReceived(packet, System.currentTimeMillis)
        } catch {
          case e: Exception =>
            listener ! PingReceiveFailed(e)
        }
      }
    } catch {
      case e: Exception =>
        Log.e("PingReceiver", "PingReceiver failure", e)
    }

  }
}

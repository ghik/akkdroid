package com.akkdroid.client

import com.typesafe.config.Config
import java.net.{InetAddress, DatagramPacket, MulticastSocket}
import akka.actor.ActorRef
import android.util.Log
import com.akkdroid.client.MembersManager.{PingReceived, PingReceiveFailed}

class PingReceiver(val config: Config, listener: ActorRef) extends Thread with PingConfig {
  private val socket = new MulticastSocket(port)
  socket.joinGroup(group)

  private val bytes = new Array[Byte](4096)

  override def run() {
    try {
      while (!socket.isClosed) {
        try {
          val dp = new DatagramPacket(bytes, bytes.length)
          socket.receive(dp)
          val remoteAddr = JavaSerializer.deserialize(bytes).asInstanceOf[InetAddress]
          listener ! PingReceived(remoteAddr, System.currentTimeMillis)
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

package com.akkdroid.client

import com.typesafe.config.Config
import java.net.{InetSocketAddress, DatagramPacket, DatagramSocket}
import akka.actor.ActorRef
import com.akkdroid.client.MembersManager.PingSendFailed

/**
 * Created with IntelliJ IDEA.
 * User: ghik
 * Date: 30.06.13
 * Time: 01:00
 * To change this template use File | Settings | File Templates.
 */
class PingSender(val config: Config, listener: ActorRef) extends Runnable with PingConfig {
  private val socket = new DatagramSocket
  private val ipbytes = JavaSerializer.serialize(ip)

  def run() {
    try {
      val packet = new DatagramPacket(ipbytes, ipbytes.length, new InetSocketAddress(group, port))
      socket.send(packet)
    } catch {
      case e: Exception =>
        listener ! PingSendFailed(e)
    }
  }
}

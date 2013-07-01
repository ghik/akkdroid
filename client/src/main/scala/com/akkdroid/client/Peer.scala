package com.akkdroid.client

import java.net.{DatagramPacket, DatagramSocket, InetAddress}

case class Peer(addr: InetAddress, nick: String = null)

object Peer {
  def receive(socket: DatagramSocket): Peer = {
    val bytes = new Array[Byte](4096)
    val dp = new DatagramPacket(bytes, bytes.length)
    socket.receive(dp)
    deserialize(bytes)
  }

  def deserialize(bytes: Array[Byte]) : Peer = {
    JavaSerializer.deserialize(bytes).asInstanceOf[Peer]
  }

  def serialize(packet: Peer): Array[Byte] = {
    JavaSerializer.serialize(packet)
  }
}

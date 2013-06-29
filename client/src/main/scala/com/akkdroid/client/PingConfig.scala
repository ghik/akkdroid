package com.akkdroid.client

import com.typesafe.config.Config
import java.net.InetAddress

trait PingConfig {
  val config: Config

  val ip = InetAddress.getByName(config.getString("akka.remote.netty.tcp.hostname"))
  val group = InetAddress.getByName(config.getString("akkdroid.view.group"))
  val port = config.getInt("akkdroid.view.port")
  val updateInterval = config.getInt("akkdroid.view.update-interval")
  val downAfter = config.getInt("akkdroid.view.down-after")
}

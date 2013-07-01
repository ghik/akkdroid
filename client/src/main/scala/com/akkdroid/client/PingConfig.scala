package com.akkdroid.client

import com.typesafe.config.Config
import java.net.InetAddress
import java.util.concurrent.atomic.AtomicReference

trait PingConfig {
  val configRef: AtomicReference[Config]

  val ip = InetAddress.getByName(configRef.get().getString("akka.remote.netty.tcp.hostname"))
  val group = InetAddress.getByName(configRef.get().getString("akkdroid.view.group"))
  val port = configRef.get().getInt("akkdroid.view.port")
  val updateInterval = configRef.get().getInt("akkdroid.view.update-interval")
  val downAfter = configRef.get().getInt("akkdroid.view.down-after")
}

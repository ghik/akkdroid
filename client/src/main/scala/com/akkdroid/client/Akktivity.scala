package com.akkdroid.client

import android.app.Activity
import android.os.Bundle
import akka.actor.{ActorSystem, Props}

class Akktivity extends Activity {
  override def onCreate(savedInstanceState: Bundle) {
    super.onCreate(savedInstanceState)

    val system = ActorSystem("mobile-system")
    val actor = system.actorOf(Props[SampleActor], name = "mobile-actor")
  }

}

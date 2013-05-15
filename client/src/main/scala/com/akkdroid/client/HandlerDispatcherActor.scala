package com.akkdroid.client

import akka.actor.Actor
import android.os.Handler

/**
 * Actor that upon receiving message sends it to Android handler where it will be processed by given function.
 *
 * @param handler handler to which all messages will be forwarded
 * @param onMessage code that will be invoked in handler for every message
 */
class HandlerDispatcherActor(handler: Handler, onMessage: Any => Any) extends Actor {
  def receive = {
    case msg => handler.post(new Runnable {
      def run() {
        onMessage(msg)
      }
    })
  }
}

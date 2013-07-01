package com.akkdroid.client

import com.typesafe.config.Config
import scala.collection.mutable
import akka.actor.{ActorRef, Actor}
import scala.concurrent.Promise
import com.akkdroid.client.MembersManager._
import android.util.Log
import java.util.concurrent.atomic.AtomicReference

class MembersManager(val configRef: AtomicReference[Config]) extends Actor with PingConfig {
  val config = configRef.get()
  private var listener: ActorRef = null
  private val members = new mutable.HashMap[Peer, Long]
  private def expireOld() {
    val minTime = System.currentTimeMillis - 1000 * downAfter
    members.retain { case (_, tstamp) => tstamp > minTime}
  }

  def receive = {
    case GetMembers(promise) =>
      expireOld()
      promise.success(members.keysIterator.toList)

    case PingReceived(packet, tstamp) =>
      expireOld()
      Log.i("MembersManager", s"Ping from $packet received at $tstamp")
      members(packet) = tstamp
      if (listener != null)
        listener ! MembersUpdated
    // pattern sponsored by extractor object defined in MembersManager companion object

    case SetMemberUpdateListener(actor) =>
      listener = actor

    case Failure(cause) =>
      Log.e("MembersManager", "failure", cause)
  }
}

object MembersManager {

  case class PingReceived(packet: Peer, tstamp: Long)

  abstract class Failure {
    val cause: Exception
  }

  // an extractor object - it represents a custom pattern that something can be matched against
  object Failure {
    def unapply(f: Failure) = Some(f.cause)
  }

  case class PingSendFailed(cause: Exception) extends Failure

  case class PingReceiveFailed(cause: Exception) extends Failure

  case class GetMembers(promise: Promise[List[Peer]])

  case class MembersUpdated()

  case class SetMemberUpdateListener(actor: ActorRef)
}

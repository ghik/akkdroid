package com.akkdroid.client

import com.typesafe.config.Config
import scala.collection.mutable
import akka.actor.{ActorRef, Actor}
import java.net.InetAddress
import scala.concurrent.Promise
import com.akkdroid.client.MembersManager._
import android.util.Log

class MembersManager(val config: Config) extends Actor with PingConfig {
  private var listener: ActorRef = null
  private val members = new mutable.HashMap[InetAddress, Long]
  private def expireOld() {
    val minTime = System.currentTimeMillis - 1000 * downAfter
    members.retain { case (_, tstamp) => tstamp > minTime}
  }

  def receive = {
    case GetMembers(promise) =>
      expireOld()
      promise.success(members.keysIterator.toList)

    case PingReceived(address, tstamp) =>
      expireOld()
      Log.i("MembersManager", s"Ping from $address received at $tstamp")
      members(address) = tstamp
      if (listener != null)
        listener ! MembersUpdated
    // pattern sponsored by extractor object defined in MembersManager companion object

    case SetListener(actor) =>
      listener = actor

    case Failure(cause) =>
      Log.e("MembersManager", "failure", cause)
  }
}

object MembersManager {

  case class PingReceived(address: InetAddress, tstamp: Long)

  abstract class Failure {
    val cause: Exception
  }

  // an extractor object - it represents a custom pattern that something can be matched against
  object Failure {
    def unapply(f: Failure) = Some(f.cause)
  }

  case class PingSendFailed(cause: Exception) extends Failure

  case class PingReceiveFailed(cause: Exception) extends Failure

  case class GetMembers(promise: Promise[List[InetAddress]])

  case class MembersUpdated()

  case class SetListener(actor: ActorRef)
}

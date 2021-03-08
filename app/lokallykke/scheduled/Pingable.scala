package lokallykke.scheduled

import akka.actor.{Actor, ActorRef}
import lokallykke.scheduled.Pingable.registerPingable
import dk.lokallykke.client.Messages.Common.ToClient.Ping
import play.api.libs.json.Json

trait Pingable {
  this : Actor =>
  protected[scheduled] implicit val pingFormat = Json.writes[Ping.type]

  val pingableId = Pingable.nextId
  def pingOut : ActorRef

  override def postStop(): Unit = {
    Pingable.unRegisterPingable(pingableId)
  }

  def ping = {
    println(s"Pinging")
    pingOut ! Json.toJson(Pingable.pingMessage)
  }

  println(s"Will register")
  registerPingable(this)
  println(s"Registered")
}

object Pingable {
  protected[scheduled] val pingMessage = Ping
  private var id = 0L
  def nextId = Pingable.synchronized {
    id += 1
    id
  }
  private val pingables = scala.collection.mutable.HashMap.empty[Long,Pingable]

  def registerPingable(pingable : Pingable) =  {
    pingables(pingable.pingableId) = pingable
  }

  def unRegisterPingable(pingableId : Long) = {
    pingables -= pingableId
  }

  def ping = {
    pingables.foreach(_._2.ping)

  }

}
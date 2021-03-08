package dk.lokallykke.client.util

import dk.lokallykke.client.Messages
import io.circe.Json
import org.scalajs.dom._
import org.scalajs.dom
import io.circe.parser._

trait WSConnector {
  import io.circe.generic.auto._
  import io.circe.syntax._

  private var ws : Option[WebSocket] = None

  def onOpen : Event => Unit = (ev : Event) =>  {
    println(s"Connection to: $location established")
  }
  def onClose : Event => Unit = (ev : Event) => {
    println(s"Connection to: $location closed")
  }
  def onError : Event => Unit = (ev : Event) => {
    println(s"Error occured")
    println(ev)
  }
  def onMessage : MessageEvent => Unit = (ev : Event) => {
    println(s"onMessage not implemented for ${this.getClass}")
  }

  def onJson : Option[Json => Unit] = None

  def ! (message : String) = {
    println(s"Sending message: $message")
    ws.foreach(_.send(message))
  }

  private def wrapOnMessage : MessageEvent => Unit = {
    case mess : MessageEvent => {
      parse(mess.data.toString) match {
        case Left(err) => {
          println(err.message)
        }
        case Right(js) => {
          js.as[Messages.Common.ToClient.Ping.type].toOption match {
            case Some(ping) =>
            case None => onJson.map(_(js)).getOrElse(onMessage(mess))
          }
        }
      }
    }
  }

  lazy val location = {
    val protocol = if(dom.document.location.protocol.toLowerCase().startsWith("https")) "wss" else "ws"
    s"$protocol://${dom.document.location.host}${dom.document.location.pathname}/ws"
  }

  def connectToServer : Unit = {
    println(s"Connecting to server on location: $location")
    val socket = new WebSocket(location)
    socket.onopen = onOpen
    socket.onclose = onClose
    socket.onerror = onError
    socket.onmessage = wrapOnMessage
    ws = Some(socket)
  }




}

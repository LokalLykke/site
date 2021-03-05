package dk.lokallykke.client.util

import dk.lokallykke.client.Locations
import org.scalajs.dom._
import org.scalajs.dom

trait WSConnector {
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
  def onMessage : MessageEvent => Unit

  def ! (message : String) = {
    println(s"Sending message: $message")
    ws.send(message)
  }

  lazy val location = {
    val protocol = if(dom.document.location.protocol.toLowerCase().startsWith("https")) "wss" else "ws"
    s"$protocol://${dom.document.location.host}${dom.document.location.pathname}/ws"
  }
  val ws = connectToServer

  def connectToServer = {
    println(s"Connecting to server on location: $location")
    val ws = new WebSocket(location)
    ws.onopen = onOpen
    ws.onclose = onClose
    ws.onerror = onError
    ws.onmessage = onMessage
    ws
  }


}

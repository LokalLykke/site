package dk.lokallykke.client.util

import dk.lokallykke.client.Locations
import org.scalajs.dom._

trait WSConnector {
  def location : String
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

  lazy val ws = connectToServer

  def connectToServer = {
    val ws = new WebSocket(location)
    ws.onopen = onOpen
    ws.onclose = onClose
    ws.onerror = onError
    ws.onmessage = onMessage
    ws
  }


}

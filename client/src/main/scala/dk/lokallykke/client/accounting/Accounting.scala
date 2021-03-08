package dk.lokallykke.client.accounting

import org.querki.jquery._
import dk.lokallykke.client.Locations
import dk.lokallykke.client.util.WSConnector
import org.scalajs.dom.{Event, WebSocket}
import org.scalajs.dom.raw._
import dk.lokallykke.client.Messages.{Accounting => AccMessages}
import AccMessages.ToClient.Pong
import dk.lokallykke.client.viewmodel.accounting.{ToClientMessage, ToServerMessage}

import scala.scalajs.js.JSON
import scala.scalajs.js.annotation.{JSExport, _}
import io.circe._
import io.circe.parser._
import org.scalajs.dom

import scala.util.Failure

@JSExportTopLevel("Accounting")
object Accounting {

  @JSExport
  def main(): Unit = {
    println(s"This is some compiled scala.js shiat for ya'll right here")
    AccountingConnector.connectToServer
  }





  object AccountingConnector extends WSConnector {
    /*implicit val pongDecoder : Decoder[Pong] = io.circe.generic.semiauto.deriveDecoder
    implicit val pongEncoder : Encoder[Pong] = io.circe.generic.semiauto.deriveEncoder*/
    import io.circe.generic.auto._
    import io.circe.syntax._


    override def onOpen: Event => Unit = {
      case ev => {
        println(s"Connected to server")
      }
    }


    override def onMessage: MessageEvent => Unit = (ev : MessageEvent) => {
      println(s"Received: ${ev.data.toString}")
      parse(ev.data.toString) match {
        case Left(fail) => println(fail.message)
        case Right(js) => js.as[ToClientMessage].foreach {
          case mess => println(s"Received message: ${mess.pong}")
        }
      }
    }

  }


}

package controllers

import akka.actor.{Actor, ActorRef, ActorSystem}
import akka.stream.Materializer
import dk.lokallykke.client.viewmodel.accounting.{ToClientMessage, ToServerMessage}
import lokallykke.scheduled.Pingable
import play.api.libs.json.Json

import javax.inject.Inject
import play.api.mvc._

class AccountingController  @Inject() (cc : ControllerComponents)(implicit inSys : ActorSystem, inMat : Materializer) extends AdminController(cc) {

  def index = actionFrom {
    case request : Request[AnyContent] => {
      Ok(views.html.accounting())
    }

  }/*Action {
    implicit request : Request[AnyContent] => {
      val toSend = ViewHoldingItem(1L, "Test item", Some("More stuff"), None)

      Ok(views.html.accounting())
    }
  }*/

  def socket = wsFrom((out : ActorRef) => new AccountingWSActor(out))


  class AccountingWSActor(out : ActorRef) extends Actor with Pingable {
    implicit val messReads = Json.reads[ToServerMessage]
    implicit val messWrites = Json.writes[ToClientMessage]


    override def receive = {
      case ToServerMessage(ping) => {
        println(s"Received ping: $ping")
        logger.info(s"Received ping: $ping")
        val toSend = ToClientMessage(s"Pong to the $ping")
        out ! Json.toJson(toSend)

      }

      case mess => {
        val txt = mess.toString
        println(s"Received message: $txt")
        val parsed = Json.parse(txt).as[ToServerMessage]
        println(s"Received ping: ${parsed.ping}")
        val toSend = ToClientMessage(s"Pong to the ${parsed.ping}")
        out ! Json.toJson(toSend)


      }
    }

    override def pingOut: ActorRef = out
  }

}

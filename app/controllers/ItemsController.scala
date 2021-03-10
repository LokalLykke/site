package controllers

import akka.actor.{Actor, ActorRef, ActorSystem}
import akka.stream.Materializer
import dk.lokallykke.client.Messages.Items._
import dk.lokallykke.client.viewmodel.items.ViewItem
import lokallykke.db.{Connection, ItemHandler}
import lokallykke.scheduled.Pingable
import play.api.libs.json.Json
import play.api.mvc._

import javax.inject.Inject

class ItemsController  @Inject()(cc : ControllerComponents)(implicit inSys : ActorSystem, inMat : Materializer) extends PageController(cc) {
  val handler = Connection.h2handler

  def index = actionFrom {
    case request : Request[AnyContent] => {
      val items = ItemsController.loadItems(handler, false)
      Ok(views.html.items(items))
    }

  }
  def socket = wsFrom((out : ActorRef) => new ItemsWSActor(out))


  class ItemsWSActor(out : ActorRef) extends Actor with Pingable {
    implicit val messReads = Json.reads[ToServer.ToServerMessage]


    override def receive = {
      case ToServer.ToServerMessage(messTyp) => {

      }

      case mess => {
        val txt = mess.toString
        println(s"Received message: $txt")
      }
    }

    override def pingOut: ActorRef = out

  }

}

object ItemsController {

  def loadItems(handler : ItemHandler, includeSold : Boolean) = {
    handler.loadItems(includeSold).map {
      case it => ViewItem(it.id, it.instagramId, it.caption, it.registered.getTime, it.costvalue, it.askprice)
    }
  }

}

package controllers

import akka.actor.{Actor, ActorRef, ActorSystem}
import akka.stream.Materializer
import dk.lokallykke.client.Messages.Items._
import dk.lokallykke.client.viewmodel.items.ViewItem
import lokallykke.db.{Connection, ItemHandler}
import lokallykke.scheduled.Pingable
import lokallykke.structure.Site
import org.slf4j.LoggerFactory
import play.api.libs.json.Json
import play.api.mvc._

import javax.inject.Inject
import scala.util.{Failure, Success, Try}

class ItemsController  @Inject()(cc : ControllerComponents, site : Site)(implicit inSys : ActorSystem, inMat : Materializer) extends PageController(cc) {

  val handler = site.itemHandler

  def index = actionFrom {
    case request : Request[AnyContent] => {
      val items = ItemsController.loadItems(handler, false)
      Ok(views.html.items(items))
    }

  }
  def socket = wsFrom((out : ActorRef) => new ItemsWSActor(out, site))


  class ItemsWSActor(out : ActorRef, site : Site) extends Actor with Pingable {
    implicit val itemReads = Json.reads[ViewItem]
    implicit val itemWrites = Json.writes[ViewItem]
    implicit val messReads = Json.reads[ToServer.ToServerMessage]
    implicit val messWrites = Json.writes[ToClient.ToClientMessage]

    def sendItems = {
      val items = ItemsController.loadItems(site.itemHandler, false)
      out ! Json.toJson(ToClient.ToClientMessage(Some(items)))
    }


    override def receive = {
      case mess => Try {
        Json.parse(mess.toString).as[ToServer.ToServerMessage] match {
          case ToServer.ToServerMessage(ToServer.UpdateItem, item) => {
            item.foreach {
              case it => {
                site.itemHandler.updateItem(it.itemId, it.name, it.caption, it.costValue, it.askPrice)
                sendItems
              }
            }
          }
          case ToServer.ToServerMessage(messTyp, _) => {
            logger.info(s"Don't know what to do with: $messTyp")
          }
        }
      } match {
        case Success(_) =>
        case Failure(err) => logger.error(s"During reception of messahe: $mess", err)
      }
    }

    override def pingOut: ActorRef = out

  }

}

object ItemsController {

  def loadItems(handler : ItemHandler, includeSold : Boolean) = {
    handler.loadItems(includeSold).map {
      case it => ViewItem(it.id, it.instagramId, it.name, it.caption, it.registered.getTime, it.costvalue, it.askprice)
    }
  }

}

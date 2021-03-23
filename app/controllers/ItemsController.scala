package controllers

import akka.actor.{Actor, ActorRef, ActorSystem}
import akka.stream.Materializer
import dk.lokallykke.client.Messages.Items.ToClient.FileUploadResult
import dk.lokallykke.client.Messages.Items._
import dk.lokallykke.client.viewmodel.items.ViewItem
import lokallykke.Cache
import lokallykke.db.{Connection, ItemHandler}
import lokallykke.instagram.LoaderObserver.ParsingState
import lokallykke.instagram.LoaderObserver.ParsingState.ParsingState
import lokallykke.instagram.{InstagramLoader, LoaderObserver}
import lokallykke.scheduled.Pingable
import lokallykke.structure.Site
import org.apache.commons.io.FileUtils
import org.slf4j.LoggerFactory
import play.api.libs.json.{Json, Reads, Writes}
import play.api.mvc._

import javax.inject.Inject
import scala.util.{Failure, Success, Try}

class ItemsController  @Inject()(cc : ControllerComponents, site : Site)(implicit inSys : ActorSystem, inMat : Materializer) extends AdminController(cc) {
  import ItemsController._

  val handler = site.itemHandler

  def index = actionFrom {
    case request : Request[AnyContent] => {
      val items = ItemsController.loadItems(handler, false)
      val options = site.itemHandler.loadDistinctTags

      Ok(views.html.items(items, options.mkString(";")))
    }

  }
  def socket = wsFrom((out : ActorRef) => new ItemsWSActor(out, site))

  def upload = Action(parse.multipartFormData) {
    implicit request => {
      val files = request.body.files.map {
        case file => {
          val bytz = FileUtils.readFileToByteArray(file.ref)
          val created = site.itemHandler.createItemFromImage(bytz)
          FileUploadResult(created.id, Some(file.filename), true)
        }
      }
      val items = ItemsController.loadItems(site.itemHandler, files.map(_.id))
      val outMessage = ToClient.ToClientMessage(uploadResult = Some(files), items = Some(items))

      Ok(Json.toJson(outMessage))
    }
  }



  class ItemsWSActor(out : ActorRef, site : Site) extends Actor with Pingable {
    import ItemsController._

    def sendItems = {
      val items = ItemsController.loadItems(site.itemHandler, false)
      val options = site.itemHandler.loadDistinctTags
      out ! Json.toJson(ToClient.ToClientMessage(Some(items), tagOptions = Some(options)))
    }


    override def receive = {
      case mess => Try {
        val message = Json.parse(mess.toString).as[ToServer.ToServerMessage]
        message.messageType match {
          case ToServer.DeleteItemAndLoad => {
            message.itemId.foreach(id => site.itemHandler.deleteItem(id))
            sendItems
          }
          case ToServer.DeleteItem => {
            message.itemId.foreach(id => site.itemHandler.deleteItem(id))
          }
          case ToServer.RequestItems => {
            sendItems
          }
          case ToServer.UpdateItemAndLoad => {
            message.viewItem.foreach {
              case it => {
                site.itemHandler.updateItem(it.itemId, it.name, it.caption, it.costValue, it.askPrice)
                site.itemHandler.updateTagsFor(it.itemId, it.tags)
                sendItems
              }
            }
          }
          case ToServer.UpdateItem => {
            message.viewItem.foreach {
              case it => {
                site.itemHandler.updateItem(it.itemId, it.name, it.caption, it.costValue, it.askPrice)
                site.itemHandler.updateTagsFor(it.itemId, it.tags)
              }
            }
          }
          case ToServer.LoadInstagramItems => {
            val loaded = ItemsController.loadFromInstagram(out)
            val existing = site.itemHandler.distinctInstagramIds
            val converted = loaded.filter(en => !existing(en.id)).map(en => ToClient.InstagramResult(en.id, en.caption, en.bytes, en.filetype, en.tags))
            val options = site.itemHandler.loadDistinctTags
            out ! Json.toJson(ToClient.ToClientMessage(instagramResults = Some(converted), tagOptions = Some(options)))
            loaded.foreach(it => Cache.InstagramImages.cache(it.id, it.bytes))
          }
          case ToServer.CreateInstagramItem => {
            message.instagramItem.foreach {
              case item => {
                Cache.InstagramImages.pop(item.instagramId).foreach {
                  case bytes => {
                    site.itemHandler.createItem(Some(item.instagramId), bytes, item.name, item.caption, item.costValue, item.askPrice)
                    val mess = ToClient.ToClientMessage(uploadedInstagramItem = Some(item.instagramId))
                    out ! Json.toJson(mess)
                  }
                }
              }
            }
          }

          case messTyp => {
            logger.info(s"Don't know what to do with: $messTyp")
          }
        }
      } match {
        case Success(_) =>
        case Failure(err) => logger.error(s"During reception of message: $mess", err)
      }
    }

    override def pingOut: ActorRef = out

  }

}

object ItemsController {

  private val instagramLock = "Locks"
  implicit val itemReads : Reads[ViewItem] = Json.reads[ViewItem]
  implicit val instagramReads : Reads[ToServer.InstagramItem] = Json.reads[ToServer.InstagramItem]
  implicit val messReads : Reads[ToServer.ToServerMessage] = Json.reads[ToServer.ToServerMessage]
  implicit val itemWrites : Writes[ViewItem] = Json.writes[ViewItem]
  implicit val fileUploadResultWrites : Writes[FileUploadResult] = Json.writes[FileUploadResult]
  implicit val instagramResultWrites : Writes[ToClient.InstagramResult] = Json.writes[ToClient.InstagramResult]
  implicit val messWrites : Writes[ToClient.ToClientMessage] = Json.writes[ToClient.ToClientMessage]

  def loadItems(handler : ItemHandler, includeSold : Boolean) = {
    val items = handler.loadItems(includeSold)
    val tags = handler.loadTagsFor(items.map(_.id)).groupBy(_.itemid).map(p => p._1 -> p._2.sortBy(_.tag).map(_.tag))
    items.map {
      case it => ViewItem(it.id, it.instagramId, it.name, it.caption, it.registered.getTime, it.costvalue, it.askprice, tags.getOrElse(it.id, Nil))
    }
  }

  def loadItems(handler : ItemHandler, itemIds : Seq[Long]) = {
    val tags = handler.loadTagsFor(itemIds).groupBy(_.itemid).map(p => p._1 -> p._2.sortBy(_.tag).map(_.tag))
    handler.loadItems(itemIds).map {
      case it => ViewItem(it.id, it.instagramId, it.name, it.caption, it.registered.getTime, it.costvalue, it.askprice, tags.getOrElse(it.id, Nil))
    }
  }

  def loadFromInstagram(out : ActorRef) = instagramLock.synchronized {
    implicit val observer = new LoaderObserver {
      override def onCommandLineUpdate(str: String): Unit = out ! Json.toJson(ToClient.ToClientMessage(instagramUpdate = Some(str)))

      override def onProgressChange(state: ParsingState): Unit = {
        val str = state match {
          case ParsingState.Downloading => "Downloading posts from Instagram"
          case ParsingState.Parsing => "Parsing downloads"
          case _ => "Finished downloading Instagram posts"
        }
        out ! Json.toJson(ToClient.ToClientMessage(instagramUpdate = Some(str)))
      }
    }
    InstagramLoader.downloadItems
  }


}

package controllers

import akka.actor.{Actor, ActorRef, ActorSystem}
import akka.stream.Materializer
import dk.lokallykke.client.Messages.Pages._
import dk.lokallykke.client.util.editor.Editor
import dk.lokallykke.client.util.editor.Editor.EditorData.Block
import dk.lokallykke.client.viewmodel.items.ViewItem
import dk.lokallykke.client.viewmodel.pages.ViewPage
import lokallykke.Cache
import lokallykke.db.PageHandler
import lokallykke.model.pages.{Page, PageContent, PageTag}
import lokallykke.scheduled.Pingable
import lokallykke.structure.Site
import org.apache.commons.io.FileUtils
import play.api.libs.json.{JsObject, Json}
import play.api.mvc._
import play.api.libs.json._
import play.api.libs.ws.WSClient

import javax.inject.Inject
import scala.concurrent.ExecutionContext
import scala.util.{Failure, Success, Try}

class PagesController  @Inject()(cc : ControllerComponents, executionContext : ExecutionContext, wsClient : WSClient, site : Site)(implicit inSys : ActorSystem, inMat : Materializer) extends AdminController(cc, executionContext, wsClient, site) {

  val handler = site.pageHandler

  def index = actionFrom {
    case (request : Request[AnyContent], context) => {
      import PagesController._
      val shells = Json.stringify(Json.toJson(handler.loadPageIdAndNames.map(p => PageShell(p._1, p._2)).sortBy(_.name)))
      val tags = handler.loadTags
      Ok(views.html.pages(shells, tags.mkString(";")))
    }

  }

  def saveImage() = actionFrom {
    case (request : Request[AnyContent], context) => {
      val (bytes, typ) = (request.body.asMultipartFormData.head.file("image").map {
        case tempFile => {
          val rb = FileUtils.readFileToByteArray(tempFile.ref)
          (rb, tempFile.contentType.get)
        }
      }).get

      val id = site.pageHandler.saveImage(bytes, typ)
      val reloadUrl = controllers.routes.PagesController.loadImage(id).url

      val resp = JsObject(
        Seq(
          "success" -> JsNumber(1),
          "file" -> JsObject(
            Seq(
              "url" -> JsString(reloadUrl)
            )
          )
        )
      )
      Ok(Json.stringify(resp))
    }
  }

  def loadImage(id : Long) = Action {
    implicit request : Request[AnyContent] => {
      val image = site.pageHandler.loadImage(id)
      Ok(image.bytes).as(image.contenttype)
    }
  }

  def socket = wsFrom((out : ActorRef) => new PagesWSActor(out, site))

  class PagesWSActor(out : ActorRef, site : Site) extends Actor with Pingable {
    import PagesController._

    def send(outMessage : ToClient.ToClientMessage) = {
      val asJson = Json.toJson(outMessage)
      out ! asJson
    }

    def sendShells = {
      val shells = handler.loadPageIdAndNames.map(p => PageShell(p._1, p._2)).sortBy(_.name)
      send(ToClient.ToClientMessage(pageShells = Some(shells)))

    }

    override def receive = {
      case mess => Try {
        val message = Json.parse(mess.toString).as[ToServer.ToServerMessage]
        message.messageType match {
          case ToServer.GetPage => {
            for(
              pid <- message.pageId;
              pag <- PagesController.loadViewPags(pid, handler)
            ) {
              val tags = handler.loadTags
              val outMessage = ToClient.ToClientMessage(page = Some(pag), tags = Some(tags))
              send(outMessage)
            }
          }
          case ToServer.DeletePage => {
            logger.info(s"Received order to delete page, from message: ${message}")
            message.pageId.foreach(p => handler.deletePage(p))
            sendShells
          }
          case ToServer.SavePage => {
            message.viewPage.foreach {
              case pag => {
                val generatedPageIdForUpdate = PagesController.saveViewPage(pag, handler)
                generatedPageIdForUpdate.foreach {
                  case genPagId => {
                    val genPagIdMess = ToClient.ToClientMessage(generatedPageId = generatedPageIdForUpdate)
                    send(genPagIdMess)
                  }
                }
                sendShells
              }
            }
          }
          case ToServer.ExecuteFilter => {
            message.tags.foreach {
              case tags => {
                val items = site.itemHandler.loadItemsMatchingTags(tags).sortBy(_.name)
                val viewItems = items.map(it => ViewItem(it.id, it.instagramId, it.name, it.caption, it.registered.getTime, it.costvalue, it.askprice, Nil))
                val mess = ToClient.ToClientMessage(items = Some(viewItems))
                send(mess)
              }
            }
          }
          case mes => logger.info(s"Got weird message: $mes")
        }

      }
        match {
        case Success(_) =>
        case Failure(err) => err.printStackTrace()
      }
    }

    override def pingOut: ActorRef = out

  }

}

object PagesController {
  implicit val blockReads : Reads[Block] = Json.reads[Block]
  implicit val viewPageReads : Reads[ViewPage] = Json.reads[ViewPage]
  implicit val toServerMessageReads : Reads[ToServer.ToServerMessage] = Json.reads[ToServer.ToServerMessage]
  implicit val pageShellWrites : Writes[PageShell] = Json.writes[PageShell]
  implicit val blockWrites : Writes[Block] = Json.writes[Block]
  implicit val viewPageWrites : Writes[ViewPage] = Json.writes[ViewPage]
  implicit val itemWrites : Writes[ViewItem] = Json.writes[ViewItem]
  implicit val toClientWrites : Writes[ToClient.ToClientMessage] = Json.writes[ToClient.ToClientMessage]


  def loadViewPags(pageId : Long, handler : PageHandler) : Option[ViewPage] = {
    handler.loadPage(pageId).map {
      case PageHandler.PageResults.LoadResult(page, tags, content, contentItems) => {
        val itemsMap = contentItems.groupBy(_.pageContentId).map {
          case (cid, ents) => cid -> ents.sortBy(_.indx).map(en => en.text)
        }
        val blocks = content.map {
          case cont => Block(cont.contenttype, cont.text, cont.level, cont.style, itemsMap.get(cont.id),
            cont.imageid.map(i => controllers.routes.PagesController.loadImage(i).url), cont.caption, cont.withborder.map(_ == 1),
            cont.stretched.map(_ == 1), cont.withbackground.map(_ == 1))
        }
        ViewPage(page.id, page.name, page.description, tags.map(_.tagname), blocks)
      }
    }
  }
  private val imageIdRegex = "[0-9]+".r
  def deriveImageId(imageUrl : String) = {
    imageIdRegex.findFirstIn(imageUrl.reverse).map(_.reverse.toLong)
  }

  def saveViewPage(viewPage : ViewPage, handler : PageHandler) : Option[Long] = {
    val page = Page(viewPage.pageId, viewPage.name, viewPage.description, 0)
    val tags = viewPage.tags.map(t => PageTag(0L, t))
    val content = viewPage.blocks.zipWithIndex.map {
      case (bl, indx) => (PageContent(0L, 0L, indx, bl.blockType, None, bl.text, bl.style, bl.level, bl.fileUrl.flatMap(url => deriveImageId(url)),
        bl.caption, bl.withBorder.map(b => if(b) 1 else 0), bl.stretched.map(b => if(b) 1 else 0), bl.withBackground.map(b => if(b) 1 else 0))
        , bl.items.toList.flatten)
    }
    handler.savePage(page, tags, content)
  }


}

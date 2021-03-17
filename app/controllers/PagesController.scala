package controllers

import akka.actor.{Actor, ActorRef, ActorSystem}
import akka.stream.Materializer
import dk.lokallykke.client.Messages.Items.ToClient.FileUploadResult
import dk.lokallykke.client.Messages.Items._
import dk.lokallykke.client.viewmodel.pages.ViewPage
import lokallykke.Cache
import lokallykke.scheduled.Pingable
import lokallykke.structure.Site
import org.apache.commons.io.FileUtils
import play.api.libs.json.Json
import play.api.mvc._

import javax.inject.Inject
import scala.util.{Failure, Success, Try}

class PagesController  @Inject()(cc : ControllerComponents, site : Site)(implicit inSys : ActorSystem, inMat : Materializer) extends PageController(cc) {

  val handler = site.itemHandler

  def index = actionFrom {
    case request : Request[AnyContent] => {
      val pages = List(
        ViewPage(1L, "Gamle ting og sager", Some("Ting der er gamle"), List("op", "ned")),
        ViewPage(2L, "Ting jeg har malet blå", Some("Blå ting"), List("op")),
        ViewPage(3L, "Skrammel", Some("Sælges for en slik"), List("ned"))
      )
      val tags = site.itemHandler.loadDistinctTags
      Ok(views.html.pages(pages, tags.mkString(";")))
    }

  }
  def socket = wsFrom((out : ActorRef) => new PagesWSActor(out, site))


  class PagesWSActor(out : ActorRef, site : Site) extends Actor with Pingable {



    override def receive = {
      case mess => Try {       }
    }

    override def pingOut: ActorRef = out

  }

}



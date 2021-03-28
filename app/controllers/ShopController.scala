package controllers

import akka.actor.{Actor, ActorRef, ActorSystem}
import akka.stream.Materializer
import dk.lokallykke.client.Messages.Pages._
import dk.lokallykke.client.viewmodel.items.ViewItem
import lokallykke.scheduled.Pingable
import lokallykke.structure.Site
import org.apache.commons.io.FileUtils
import play.api.libs.json.{JsObject, Json, _}
import play.api.mvc._

import javax.inject.Inject
import scala.util.{Failure, Success, Try}

class ShopController  @Inject()(cc : ControllerComponents, site : Site)(implicit inSys : ActorSystem, inMat : Materializer) extends VisitorController(cc) {

  val handler = site.pageHandler

  def index = actionFrom {
    case request : Request[AnyContent] => {
      val pages = site.customerPageHandler.loadCustomerPages
      val carouselItems = site.itemHandler.loadItemsMatchingTags(List("forside"))
      Ok(views.html.shop(pages, carouselItems))
    }

  }

  def socket = wsFrom((out : ActorRef) => new ShopWSActor(out, site))

  class ShopWSActor(out : ActorRef, site : Site) extends Actor with Pingable {

    override def receive = {
      case mess => Try {
      }
    }

    override def pingOut: ActorRef = out

  }

}



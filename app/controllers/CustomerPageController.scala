package controllers

import akka.actor.{Actor, ActorRef, ActorSystem}
import akka.stream.Materializer
import dk.lokallykke.client.viewmodel.customer.CustomerPage
import lokallykke.scheduled.Pingable
import lokallykke.structure.Site
import play.api.mvc._

import javax.inject.Inject
import scala.util.Try

class CustomerPageController  @Inject()(cc : ControllerComponents, site : Site)(implicit inSys : ActorSystem, inMat : Materializer) extends VisitorController(cc, site) {

  val handler = site.pageHandler

  def page(pageId : Long) = actionFrom {
    case request : Request[AnyContent] => {
      site.customerPageHandler.loadCustomerPageAndContent(pageId) match {
        case None => NotFound
        case Some((pag, conts)) => {
          Ok(views.html.customerpage(super.customerPages, pag, conts))
        }
      }
    }
  }

  def socket = wsFrom((out : ActorRef) => new CustomerPageWSActor(out, site))

  class CustomerPageWSActor(out : ActorRef, site : Site) extends Actor with Pingable {

    override def receive = {
      case mess => Try {
      }
    }

    override def pingOut: ActorRef = out

  }

}


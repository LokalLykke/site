package controllers

import akka.actor.{Actor, ActorRef, ActorSystem, Props}
import akka.stream.Materializer
import org.slf4j.LoggerFactory
import play.api.libs.json.JsObject
import play.api.libs.streams.ActorFlow
import play.api.mvc._

import javax.inject.Inject
import scala.concurrent.Future

abstract class VisitorController @Inject()(cc : ControllerComponents)(implicit inSys : ActorSystem, inMat : Materializer) extends AbstractController(cc) {
  protected lazy val logger = LoggerFactory.getLogger(this.getClass)

  def actionFrom(act : Request[AnyContent] => Result) = {
    val composed = act.compose( {
      case req : Request[AnyContent] => {
        log(req);
        req
      }
    })
    Action { act }
  }

  def wsFrom(actorCreator : (ActorRef) => Actor) = WebSocket.acceptOrResult[Any, JsObject] {
    case request => {
      log(request)
      Future.successful {
        Right(ActorFlow.actorRef {
          case out => Props(actorCreator(out))
        } )
      }
    }
  }

  protected def log(req : Request[AnyContent]) : Unit = {
    logRemoteAddress(req.remoteAddress)
  }

  protected def log(req : RequestHeader) : Unit = {
    logRemoteAddress(req.remoteAddress)
  }

  private def logRemoteAddress(address : String) : Unit = {
    println(s"Got a visit from : $address")
  }



}

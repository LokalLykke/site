package controllers

import akka.actor.{Actor, ActorRef, ActorSystem, Props}
import akka.stream.Materializer
import org.slf4j.LoggerFactory
import play.api.libs.json.JsObject
import play.api.libs.streams.ActorFlow
import play.api.mvc._

import javax.inject.Inject
import scala.concurrent.Future

abstract class AdminController @Inject()(cc : ControllerComponents)(implicit inSys : ActorSystem, inMat : Materializer) extends AbstractController(cc) {
  protected lazy val logger = LoggerFactory.getLogger(this.getClass)

  def actionFrom(act : Request[AnyContent] => Result) = {
    Action { act }
  }

  def wsFrom(actorCreator : (ActorRef) => Actor) = WebSocket.acceptOrResult[Any, JsObject] {
    case request => {
      Future.successful {
        Right(ActorFlow.actorRef {
          case out => Props(actorCreator(out))
        } )
      }
    }
  }



}

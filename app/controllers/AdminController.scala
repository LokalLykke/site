package controllers

import akka.actor.{Actor, ActorRef, ActorSystem, Props}
import akka.http.javadsl.model.ContentType
import akka.stream.Materializer
import lokallykke.LocallykkeConfig
import lokallykke.security.{Encryption, GoogleAuthenticator, StateValueGenerator}
import lokallykke.structure.Site
import controllers.routes
import org.slf4j.LoggerFactory
import play.api.http.ContentTypes
import play.api.libs.json.JsObject
import play.api.libs.streams.ActorFlow
import play.api.libs.ws.WSClient
import play.api.mvc._

import java.net.URLEncoder
import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

abstract class AdminController @Inject()(cc : ControllerComponents, executionContext : ExecutionContext, wsClient : WSClient, site : Site)(implicit inSys : ActorSystem, inMat : Materializer) extends AbstractController(cc) {
  protected lazy val logger = LoggerFactory.getLogger(this.getClass)
  private val sessionHandler = site.sessionHandler
  private implicit val ec = executionContext

  def actionFrom(act : (Request[AnyContent], AdminController.AdminControllerContext) => Result) : Action[AnyContent] = Action.async {
    request: Request[AnyContent] =>
      authenticate(request) match {
        case Left(fut) => fut
        case Right(contx) => Future {
          act(request, contx)
        }
      }
  }

  def authenticate(request : Request[AnyContent]) : Either[Future[Result], AdminController.AdminControllerContext] = {
    implicit val req = request
    val ip = request.connection.remoteAddress.getHostAddress
    val forwardUrl = request.uri
    request.cookies.get(AdminController.AdminSessionCookie) match {
      case Some(cook) if cook.value.matches("^[0-9]+$") => {
        val sessionId = cook.value.toLong
        if(sessionHandler.isAuthorized(sessionId, ip)) Right(AdminController.AdminControllerContext(sessionId))
        else Left(initiateAuthenticationFlow(Some(sessionId),ip,forwardUrl))
      }
      case _ => Left(initiateAuthenticationFlow(None, ip, forwardUrl))
    }
  }

  def initiateAuthenticationFlow(sessionId : Option[Long], ip : String, forwardUrl : String)(implicit req : Request[AnyContent]) = {
    val sessId = sessionId.getOrElse(sessionHandler.createSession(ip))
    var nonce = StateValueGenerator.generateNonce
    while(sessionHandler.nonceExists(sessId, nonce))
      nonce = StateValueGenerator.generateNonce
    val authenticator = new GoogleAuthenticator(wsClient)
    val state = Encryption.serializeAndEncrypt(List(AdminController.StateFieldSessionIdName -> sessId.toString, AdminController.StateFieldIpName -> ip, AdminController.StateFieldNonceName -> nonce))
    logger.info(s"Sending state: $state")
    val callbackUrl = AdminController.urlToCallback
    logger.info(s"Directing to forward URL: $callbackUrl")
    site.sessionHandler.initiateAuthenticationProcess(sessId, nonce, state, forwardUrl)

    authenticator.initializationURL(LocallykkeConfig.OpenID.clientId, callbackUrl, nonce, state) match {
      case Some(rediectURL) => Future { Redirect(rediectURL) }
      case None => Future { Results.ExpectationFailed }
    }


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

object AdminController {
  case class AdminControllerContext(sessionId : Long)
  def urlToCallback(implicit request : Request[_]) = routes.AuthenticationCallbackController.callback("","").absoluteURL(true).replaceAll("""\?.*""","")

  val AdminSessionCookie = "SESSIONID"
  val StateFieldSessionIdName = "SessionId"
  val StateFieldIpName = "Ip"
  val StateFieldNonceName = "Nonce"

}


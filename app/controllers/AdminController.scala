package controllers

import akka.actor.{Actor, ActorRef, ActorSystem, Props}
import akka.stream.Materializer
import lokallykke.LocallykkeConfig
import lokallykke.security.{Encryption, GoogleAuthenticator, StateValueGenerator}
import lokallykke.structure.Site
import org.slf4j.LoggerFactory
import play.api.libs.json.JsObject
import play.api.libs.streams.ActorFlow
import play.api.libs.ws.WSClient
import play.api.mvc._

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

abstract class AdminController @Inject()(cc : ControllerComponents, executionContext : ExecutionContext, wsClient : WSClient, site : Site)(implicit inSys : ActorSystem, inMat : Materializer) extends AbstractController(cc) {
  protected lazy val logger = LoggerFactory.getLogger(this.getClass)
  private val sessionHandler = site.sessionHandler
  private implicit val ec = executionContext

  def actionFrom(act : (Request[AnyContent], AdminController.AdminControllerContext) => Result) : Action[AnyContent] = {
    val handler : Action[AnyContent] = {
      case request: Request[AnyContent] => authenticate(request) match {
        case Left(fut) => Action.async {
          fut
        }
        case Right(contx) => Action {
          act(request, contx)
        }
      }
    }
    handler
  }

  def authenticate(request : Request[AnyContent]) : Either[Future[Result], AdminController.AdminControllerContext] = {
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

  def initiateAuthenticationFlow(sessionId : Option[Long], ip : String, forwardUrl : String) = {
    val sessId = sessionId.getOrElse(sessionHandler.createSession(ip))
    var nonce = StateValueGenerator.generateNonce
    while(sessionHandler.nonceExists(sessId, nonce))
      nonce = StateValueGenerator.generateNonce
    val authenticator = new GoogleAuthenticator(wsClient)
    val state = Encryption.serializeAndEncrypt(List(AdminController.StateFieldSessionIdName -> sessId.toString, AdminController.StateFieldIpName -> ip, AdminController.StateFieldNonceName -> nonce))
    authenticator.initializeFlow(LocallykkeConfig.OpenID.clientId, "", nonce, state) match {
      case Some(signinFut) => signinFut.map(signinRes => Ok(signinRes.body))
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

  val AdminSessionCookie = "SESSIONID"
  val StateFieldSessionIdName = "SessionId"
  val StateFieldIpName = "Ip"
  val StateFieldNonceName = "Nonce"

}


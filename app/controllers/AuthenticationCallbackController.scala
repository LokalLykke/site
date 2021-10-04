package controllers

import akka.actor.ActorSystem
import akka.stream.Materializer
import lokallykke.LocallykkeConfig
import lokallykke.security.{Encryption, GoogleAuthenticator}
import lokallykke.structure.Site
import play.api.libs.json.Json
import play.api.libs.ws.WSClient
import play.api.mvc.{AbstractController, Action, AnyContent, ControllerComponents, Request, Result, Results}


import javax.inject.Inject
import scala.concurrent.ExecutionContext

class AuthenticationCallbackController @Inject()(cc : ControllerComponents, executionContext : ExecutionContext, wsClient : WSClient, site : Site)(implicit inSys : ActorSystem, inMat : Materializer) extends AbstractController(cc){
  implicit val ec = executionContext

  def callback(state : String, code : String) : Action[AnyContent] = {
    case request : Request[AnyContent] => {
      val stateMap = Encryption.decryptAndDeserialize(state).toMap
      (stateMap.get(AdminController.StateFieldSessionIdName), stateMap.get(AdminController.StateFieldIpName), stateMap.get(AdminController.StateFieldNonceName)) match {
        case (Some(sessIdStr), Some(ip), Some(nonce)) if site.sessionHandler.validateNonceAndState(sessIdStr.toLong, nonce, state) => {
          val sessionId = sessIdStr.toLong
          val auther = new GoogleAuthenticator(wsClient)
          auther.exchangeCode(code, LocallykkeConfig.OpenID.clientId, LocallykkeConfig.OpenID.secret, null)
          Ok("Ok")
        }
        case _ => Results.ExpectationFailed
      }
    }
  }

  def exchangeCodeCallback() : Action[AnyContent] =  {
    case request : Request[AnyContent] => {
      request.body.asJson match {
        case Some(js) => {
          val accessToken = (js \ "access_token").as[String]
          val expiresIn = (js \ "expires_in").as[String]
          val idToken = (js\"id_token").as[String]
          val scope = (js \ "scope").as[String]
          Ok("Ok")
        }
        case _ => Results.ExpectationFailed
      }
    }
  }



}


package controllers

import akka.actor.ActorSystem
import akka.stream.Materializer
import lokallykke.LocallykkeConfig
import lokallykke.security.{Encryption, GoogleAuthenticator}
import lokallykke.structure.Site
import org.slf4j.LoggerFactory
import play.api.libs.json.Json
import play.api.libs.ws.WSClient
import play.api.mvc.{AbstractController, Action, AnyContent, ControllerComponents, Request, Result, Results}

import javax.inject.Inject
import scala.concurrent.ExecutionContext

class AuthenticationCallbackController @Inject()(cc : ControllerComponents, executionContext : ExecutionContext, wsClient : WSClient, site : Site)(implicit inSys : ActorSystem, inMat : Materializer) extends AbstractController(cc){
  implicit val ec = executionContext
  private val logger = LoggerFactory.getLogger(this.getClass)

  def callback(state : String, code : String) : Action[AnyContent] = Action {
    implicit request : Request[AnyContent] => {
      logger.info(s"Got request and with state: $state and code: $code")
      val stateMap = Encryption.decryptAndDeserialize(state).toMap
      logger.info(s"State map: ${stateMap.toList.map(p => p._1 + ":" + p._2).mkString("\r\n")}")
      (stateMap.get(AdminController.StateFieldSessionIdName), stateMap.get(AdminController.StateFieldIpName), stateMap.get(AdminController.StateFieldNonceName)) match {
        case (Some(sessIdStr), Some(ip), Some(nonce)) if site.sessionHandler.validateNonceAndState(sessIdStr.toLong, nonce, state) => {
          val sessionId = sessIdStr.toLong
          val auther = new GoogleAuthenticator(wsClient)
          val clientID = LocallykkeConfig.OpenID.clientId
          val clientSecret = LocallykkeConfig.OpenID.secret
          val redirectURL = controllers.routes.AuthenticationCallbackController.exchangeCodeCallback.absoluteURL(true)
          auther.exchangeCodeURL(code, clientID, clientSecret, redirectURL) match {
            case None => Results.ExpectationFailed
            case Some(url) => Redirect(url)
          }

          /*auther.exchangeCode(code, LocallykkeConfig.OpenID.clientId, LocallykkeConfig.OpenID.secret, controllers.routes.AuthenticationCallbackController.exchangeCodeCallback.absoluteURL(true))
          Ok("Ok")*/

        }
        case _ => Results.ExpectationFailed
      }
    }
  }

  /*
  For request 'GET /auth/callback?state=N+hPvvAEq6g7WI28IcykBy7Xxm1kFdCkGBw3RyoZee%2FkdgoyTpGZP8hozJZVnA++QgKNxgHwCGI+J2ukEgfakwLncTYdOYqGvtjiEMzmxF1WXsNlwPwRtvnzDWIbsHm4&code=4%2F0AX4XfWjlViq73cTPtd1lj-_ttAMVZZ_MWigrtt5Hs0P7waYOOxUjZnxu7j6ZLaHuu0ENiw&scope=email+openid+https%3A%2F%2Fwww.googleapis.com%2Fauth%2Fuserinfo.email&authuser=0&prompt=consent'


   */

  def exchangeCodeCallback() : Action[AnyContent] =  Action {
    request : Request[AnyContent] => {
      request.body.asJson match {
        case Some(js) => {
          val accessToken = (js \ "access_token").as[String]
          val expiresIn = (js \ "expires_in").as[String]
          val idToken = (js\"id_token").as[String]
          val scope = (js \ "scope").as[String]
          logger.info(s"Got exchange code callback. accessToken: $accessToken, expiresIn: $expiresIn, idToken: $idToken, scope: $scope")
          Ok("Ok")
        }
        case _ => Results.ExpectationFailed
      }
    }
  }



}


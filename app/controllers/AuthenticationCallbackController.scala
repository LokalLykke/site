package controllers

import akka.actor.ActorSystem
import akka.stream.Materializer
import lokallykke.LocallykkeConfig
import lokallykke.security.Encryption.JWTToken
import lokallykke.security.{Encryption, GoogleAuthenticator}
import lokallykke.structure.Site
import org.slf4j.LoggerFactory
import play.api.libs.json.Json
import play.api.libs.ws.WSClient
import play.api.mvc.{AbstractController, Action, AnyContent, ControllerComponents, Cookie, Request, Result, Results}

import java.sql.Timestamp
import java.time.LocalDateTime
import javax.inject.Inject
import scala.concurrent.{Await, ExecutionContext}
import scala.concurrent.duration._

class AuthenticationCallbackController @Inject()(cc : ControllerComponents, executionContext : ExecutionContext, wsClient : WSClient, site : Site)(implicit inSys : ActorSystem, inMat : Materializer) extends AbstractController(cc){
  implicit val ec = executionContext
  private val logger = LoggerFactory.getLogger(this.getClass)

  def callback(state : String, code : String) : Action[AnyContent] = Action {
    implicit request : Request[AnyContent] => {
      logger.debug(s"Got request and with state: $state and code: $code")
      val stateMap = Encryption.decryptAndDeserialize(state).toMap
      logger.debug(s"State map: ${stateMap.toList.map(p => p._1 + ":" + p._2).mkString("\r\n")}")
      (stateMap.get(AdminController.StateFieldSessionIdName), stateMap.get(AdminController.StateFieldIpName), stateMap.get(AdminController.StateFieldNonceName)) match {
        case (Some(sessIdStr), Some(ip), Some(nonce)) if site.sessionHandler.validateNonceAndState(sessIdStr.toLong, nonce, state) => {
          logger.info(s"Extracted from state map on callback: sessionId: $sessIdStr, IP: $ip, nonce: $nonce")
          val sessionId = sessIdStr.toLong
          val auther = new GoogleAuthenticator(wsClient)
          val clientID = LocallykkeConfig.OpenID.clientId
          val clientSecret = LocallykkeConfig.OpenID.secret
          auther.exchangeCode(code, clientID, clientSecret,AdminController.urlToCallback) match  {
            case Some(fut) => {
              val res = Await.result(fut, 10.seconds)
              val jwt = Encryption.decryptJWTToken(res.idToken)
              logger.info(s"Extract JWT token from code given on callback: ${jwt}")
              jwt match {
                case JWTToken(_, _, _, Some(email), Some(true), issuedAt, expiresAt, Some(jwtNonce)) if expiresAt.isAfter(LocalDateTime.now())
                    && issuedAt.isBefore(LocalDateTime.now())
                    && LocallykkeConfig.adminUsers.map(_.toLowerCase).contains(email.toLowerCase) => {
                  logger.info(s"Successfully authenticated user: ${email} on session ID: $sessionId")
                  val sessionCookie = Cookie(AdminController.AdminSessionCookie, sessionId.toString)
                  val validUntil = Timestamp.valueOf(expiresAt.plusDays(1L))
                  site.sessionHandler.finalizeProcess(sessionId, nonce, email, validUntil)
                  site.sessionHandler.retrieveForwardURL(sessionId, nonce) match {
                    case None => Redirect(controllers.routes.ItemsController.index()).withCookies(sessionCookie).bakeCookies()
                    case Some(url) => Redirect(url).withCookies(sessionCookie).bakeCookies()
                  }
                }

                case JWTToken(issuer, audience, subject, email, emailVerified, issuedAt, expiresAt, jwtNonce) => {
                  logger.info(s"Not all requirements for successful authentication are met")
                  logger.info(s"  email: $email, email verified: $emailVerified, issued at: ${issuedAt.toString}, expires: ${expiresAt}")
                  Results.Forbidden
                }

              }
            }
            case None => {
              logger.error(s"Unable to generate exchange code future")
              Results.Forbidden
            }
          }

        }
        case _ => {
          logger.info(s"Could not match state map: ${stateMap.toList.map(p => p._1 + ":" + p._2).mkString(",")}")
          Results.Forbidden
        }
      }
    }
  }



}


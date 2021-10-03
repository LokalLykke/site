import lokallykke.security.{GoogleAuthenticator, StateValueGenerator}
import play.api.test._

import java.net.URLEncoder
import scala.concurrent.duration._
import scala.concurrent.{Await, ExecutionContext}

object ExploreAuthentication {

  val DiscoveryUrl = """https://accounts.google.com/.well-known/openid-configuration"""

  val ClientId = """"""
  val ClientSecret = """"""

  def main(args: Array[String]): Unit = {
    discoverAuthorizationEndpoint
  }

  def discoverAuthorizationEndpoint = {
    WsTestClient.withClient {
      client => {
        implicit val executionContext = ExecutionContext.global
        implicit val cl = client
        //val res = Await.result(client.url(DiscoveryUrl).get(), 1.minute)
        //println(res.body)
        val redirectUrl = "http://localhost"
        val auther = new GoogleAuthenticator(client)
        val nonce = StateValueGenerator.generateNonce
        val state = StateValueGenerator.stateFrom(Some(System.currentTimeMillis()))
        auther.initializeFlow(ClientId, redirectUrl, nonce, state).foreach {
          case fut => {
            val res = Await.result(fut, 1.minute)
            println(res)
          }
        }
      }
    }

  }

}

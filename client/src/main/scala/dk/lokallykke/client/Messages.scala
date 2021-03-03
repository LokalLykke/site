package dk.lokallykke.client

import scala.scalajs.js.JSON

object Messages {

  object Accounting {
    object ToClient {
      case class Pong(str : String)
    }

    object ToServer {
      case class Ping(str : String)
    }


  }


}

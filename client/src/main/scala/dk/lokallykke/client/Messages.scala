package dk.lokallykke.client


import scala.scalajs.js.JSON

object Messages {

  object Common {
    object ToClient {
      case object Ping
    }
  }

  object Accounting {
    object ToClient {
      case class Pong(str : String)
    }

    object ToServer {
      case class Ping(str : String)
    }
  }

  object Items {
    import dk.lokallykke.client.viewmodel.items._

    object ToServer {
      case class ToServerMessage(messageType : String)
    }

    object ToClient {
      sealed trait ToClientMessage
      case class RefreshItems(items : Seq[ViewItem])
    }

  }


}

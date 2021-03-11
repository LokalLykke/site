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
      case class ToServerMessage(messageType : String, viewItem : Option[ViewItem])

      val UpdateItem = "UpdateItem"
    }

    object ToClient {
      case class ToClientMessage(items : Option[Seq[ViewItem]])
    }

  }


}

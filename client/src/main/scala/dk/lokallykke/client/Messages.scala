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
      case class ToServerMessage(messageType : String, viewItem : Option[ViewItem] = None, itemId : Option[Long] = None, instagramItem: Option[InstagramItem] = None)

      val UpdateItem = "UpdateItem"
      val UpdateItemAndLoad = "UpdateItemAndLoad"
      val RequestItems = "RequestItems"
      val DeleteItem = "DeleteItem"
      val DeleteItemAndLoad = "DeleteItemAndLoad"
      val LoadInstagramItems = "LoadInstagramItems"
      val CreateInstagramItem = "CreateInstagramItem"

      case class InstagramItem(instagramId : String, name : Option[String], caption : Option[String], costValue : Option[Double], askPrice : Option[Double])

    }

    object ToClient {
      case class ToClientMessage(items : Option[Seq[ViewItem]] = None, uploadResult : Option[Seq[FileUploadResult]] = None, instagramUpdate : Option[String] = None,
                                 instagramResults: Option[Seq[InstagramResult]] = None, uploadedInstagramItem : Option[String] = None)

      case class FileUploadResult(id : Long, fileName : Option[String], success : Boolean)
      case class InstagramResult(instagramId : String, caption : Option[String], bytes : Array[Byte], fileType : String)



    }

  }


}

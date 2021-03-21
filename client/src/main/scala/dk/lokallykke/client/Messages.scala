package dk.lokallykke.client


import dk.lokallykke.client.viewmodel.items.ViewItem
import dk.lokallykke.client.viewmodel.pages.ViewPage

import scala.scalajs.js
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

      case class InstagramItem(instagramId : String, name : Option[String], caption : Option[String], costValue : Option[Double], askPrice : Option[Double], tags : Seq[String])

    }

    object ToClient {
      case class ToClientMessage(items : Option[Seq[ViewItem]] = None, uploadResult : Option[Seq[FileUploadResult]] = None, instagramUpdate : Option[String] = None,
                                 instagramResults: Option[Seq[InstagramResult]] = None, uploadedInstagramItem : Option[String] = None, tagOptions : Option[Seq[String]] = None)

      case class FileUploadResult(id : Long, fileName : Option[String], success : Boolean)
      case class InstagramResult(instagramId : String, caption : Option[String], bytes : Array[Byte], fileType : String, tags : Seq[String])



    }

  }

  object Pages {
    object ToServer {
      case class ToServerMessage(messageType : String, viewPage : Option[ViewPage] = None, pageId : Option[Long] = None, tags : Option[Seq[String]] = None)

      val GetPage = "GetPage"
      val SavePage = "SavePage"
      val DeletePage = "DeletePage"
      val ExecuteFilter = "ExecuteFilter"

    }

    case class PageShell(id : Long, name : String)

    trait PageTrait extends js.Object {
      def id : Long
      def name : String
    }

    object ToClient {
      case class ToClientMessage(tags : Option[Seq[String]] = None, pageShells : Option[Seq[PageShell]] = None,
                                 page : Option[ViewPage] = None, errorMessage : Option[String] = None, items : Option[Seq[ViewItem]] = None)
    }


  }


}

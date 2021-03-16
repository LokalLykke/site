package dk.lokallykke.client.items

import dk.lokallykke.client.Locations
import dk.lokallykke.client.Messages.Items.ToClient.{FileUploadResult, InstagramResult, ToClientMessage}
import dk.lokallykke.client.Messages.Items.{ToClient, ToServer}
import dk.lokallykke.client.Messages.Items.ToServer.ToServerMessage
import dk.lokallykke.client.viewmodel.items.ViewItem
import org.querki.jquery.{$, ElementDesc, EventHandler, JQueryAjaxSettings, JQueryEventObject, JQueryXHR}
import dk.lokallykke.client.util.JsExtensions._
import dk.lokallykke.client.util.{CommonUtil, Modal, Selector, WSConnector}
import dk.lokallykke.client.util.tables.{Column, TableBuilder}
import io.circe.generic.semiauto.deriveDecoder
import io.circe.{Decoder, Json, JsonObject}
import org.scalajs.dom.raw.{File, FormData, HTMLFormElement}
import org.scalajs.dom.{Element, MessageEvent}

import java.time.LocalDateTime
import scala.scalajs.js
import scala.scalajs.js.Object.entries
import scala.scalajs.js.{Date, Dictionary, JSON, UndefOr}
import scala.scalajs.js.annotation.{JSExport, JSExportTopLevel, JSImport}
import scala.util.{Failure, Success, Try}


@JSExportTopLevel("Items")
object Items {
  import dk.lokallykke.client.util.JQueryExtensions._
  val ItemsContentId = "items-content"
  val ItemsContentSubId = "items-content-sub"
  val ItemsNavQuery = "[typ='items-nav']"
  val ItemsNavUploadId = "items-nav-upload"
  val ItemsNavOnStockId = "items-nav-on-stock"
  val ItemsNavSoldId = "items-nav-sold"
  val ItemsNavAllId = "items-nav-all"
  val ItemsUploadBoxId = "items-upload-box"
  val ItemsFromInstagramId = "items-nav-instagram"
  val ItemsFileInputId = "items-files-input"
  val ItemsInstagramTextareaId = "items-instagram-textarea"

  var uploadedFiles = Seq.empty[UploadedFile]
  var instagramItems = Seq.empty[InstagramItem]


  object Tables {
    import Modal._
    import Column._
    object ItemTable {
      val imageCol = ImageColumn[ViewItem]("image", "Billede", None, en => Some(Locations.Items.itemImage(en.itemId)))
      val nameCol = StringColumn[ViewItem]("name", "Navn",_.name)
      val captionCol = StringColumn[ViewItem]("caption", "Beskrivelse",_.caption)
      val registeredCol = DateTimeColumn[ViewItem]("registered", "Registreret", en => Some(en.registered.toDateTime))
      val costValueCol = DoubleColumn[ViewItem]("costvalue", "Købsværdi", en => en.costValue )
      val askPriceCol = DoubleColumn[ViewItem]("askprice", "Til salg for", en => en.askPrice)
      val deleteCol = ButtonColumn[ViewItem]("delete", "Slet", "Slet", Some((obj : JQueryEventObject, en : ViewItem) =>  {
        Modal.Accept("Vil du virkeligt slette?", s"Er du sikker på at du vil slette: ${en.name.getOrElse(s"genstand med ID: ${en.itemId}")}",() => {
          ItemsConnector.send(ToServer.ToServerMessage(ToServer.DeleteItemAndLoad, itemId = Some(en.itemId)))
        })
      }), inClasses = Some((a : ViewItem) => "btn btn-danger"))
      val columns = Seq(imageCol, nameCol, captionCol, registeredCol, costValueCol, askPriceCol, deleteCol)

      def rowHandlerFor(item : ViewItem) : Option[EventHandler] =  Some(((obj : JQueryEventObject) => {
        import dk.lokallykke.client.util.Modal
        import org.scalajs.dom

        val modalContents = List(
          Modal.Image("item-image",Locations.Items.itemImage(item.itemId)),
          Modal.EditableString("item-name", "Navn", item.name),
          Modal.EditableString("item-caption", "Beskrivelse", item.caption),
          Modal.EditableDouble("item-costval", "Købsværdi", item.costValue),
          Modal.EditableDouble("item-askprice", "Til salg for", item.askPrice),
          Modal.SelectableOptions("item-test-tags", "Mærkater", List("sune", "slagelse", "vakkelvorn"), Some(Seq("sune")))
        )
        Modal("item-modal", "Redigér genstand", modalContents, Some((ret) => {
          var updated = item
          ret.map(p => p._1 -> p._2()).foreach {
            case ("item-name", n : Option[String]) => updated = updated.copy(name = n)
            case ("item-caption", n : Option[String]) => updated = updated.copy(caption = n)
            case ("item-costval", d : Option[Double]) => updated = updated.copy(costValue = d)
            case ("item-askprice", d : Option[Double]) => updated = updated.copy(askPrice = d)
            case (itId,v) => {
              println(s"Found no way to update field: ${itId} with value: $v")
            }
          }
          ItemsConnector.sendViewItemUpdate(updated)
        }))
      }))
      val tableBuilder = TableBuilder[ViewItem]("item-table", columns, inRowHandler = Some(rowHandlerFor))
    }

    object UploadedFileTable {
      val imageCol = ImageColumn[UploadedFile]("image", "Billede", None, en => Some(Locations.Items.itemImage(en.viewItem.itemId)))
      val fileNameCol = StringColumn[UploadedFile]("filename", "Filnavn",_.fileName)
      val nameCol = StringColumn[UploadedFile]("name", "Navn",_.viewItem.name)
      val captionCol = StringColumn[UploadedFile]("caption", "Beskrivelse",_.viewItem.caption)
      val costValueCol = DoubleColumn[UploadedFile]("costvalue", "Købsværdi", en => en.viewItem.costValue )
      val askPriceCol = DoubleColumn[UploadedFile]("askprice", "Til salg for", en => en.viewItem.askPrice )
      val deleteCol = ButtonColumn[UploadedFile]("delete", "Slet", "Slet", Some((obj : JQueryEventObject, en : UploadedFile)  => {
        uploadedFiles = uploadedFiles.filter(_.itemid != en.itemid)
        show()
        ItemsConnector.send(ToServer.ToServerMessage(ToServer.DeleteItem, itemId = Some(en.itemid)))
      }), inClasses = Some((a : UploadedFile) => "btn btn-danger btn-sm"))

      val columns = Seq(imageCol, fileNameCol, nameCol, captionCol, costValueCol, askPriceCol, deleteCol)

      def rowHandlerFor(item : UploadedFile) : Option[EventHandler] =  Some(((obj : JQueryEventObject) => {
        import dk.lokallykke.client.util.Modal
        import org.scalajs.dom

        val modalContents = List(
          Modal.Image("item-image",Locations.Items.itemImage(item.itemid)),
          Modal.EditableString("item-name", "Navn", item.viewItem.name),
          Modal.EditableString("item-caption", "Beskrivelse", item.viewItem.name),
          Modal.EditableDouble("item-costval", "Købsværdi", item.viewItem.costValue),
          Modal.EditableDouble("item-askprice", "Til salg for", item.viewItem.askPrice)
        )
        Modal("item-modal", "Redigér genstand", modalContents, Some((ret) => {
          var updated = item.viewItem
          ret.map(p => p._1 -> p._2()).foreach {
            case ("item-name", n : Option[String]) => updated = updated.copy(name = n)
            case ("item-caption", n : Option[String]) => updated = updated.copy(caption = n)
            case ("item-costval", d : Option[Double]) => updated = updated.copy(costValue = d)
            case ("item-askprice", d : Option[Double]) => updated = updated.copy(askPrice = d)
            case (itId,v) => {
              println(s"Found no way to update field: ${itId} with value: $v")
            }
          }
          uploadedFiles = uploadedFiles.map(en => if(en.itemid == item.itemid) item.copy(viewItem = updated) else en)
          show()
          ItemsConnector.send(ToServer.ToServerMessage(ToServer.UpdateItem, Some(updated)))
        }))
      }))
      val tableBuilder = TableBuilder[UploadedFile]("item-uploaded-file", columns, inRowHandler = Some(rowHandlerFor), imageSize = 40, inTableClass = "table table-hover table-sm")

      def show() : Unit = {
        $(s"#$ItemsContentSubId").empty()
        val tab = tableBuilder.buildTable(uploadedFiles)
        $(s"#$ItemsContentSubId").append(tab)
      }


    }

    object InstagramTable {
      val imageCol = ImageColumn[InstagramItem]("image", "Billede", bytesFor = (en : InstagramItem) => en.instagramResult.bytes, fileTypeFor =  (en : InstagramItem) => en.instagramResult.fileType, None)
      val fileNameCol = StringColumn[InstagramItem]("name", "Navn",_.name)
      val nameCol = StringColumn[InstagramItem]("caption", "Beskrivelse",_.captionToUse)
      val costValueCol = DoubleColumn[InstagramItem]("costvalue", "Købsværdi", en => en.costValue )
      val askPriceCol = DoubleColumn[InstagramItem]("askprice", "Til salg for", en => en.askPrice )
      val importCol = ButtonColumn[InstagramItem]("create-item", "Importér", "Importér", Some((obj : JQueryEventObject, en : InstagramItem)  => {
        val toSend = ToServer.ToServerMessage(ToServer.CreateInstagramItem, instagramItem = Some(ToServer.InstagramItem(en.instagramId , en.name, en.captionToUse, en.costValue, en.askPrice)))
        ItemsConnector.send(toSend)

      }), inClasses = Some((a : InstagramItem) => "btn btn-secondary btn-sm"))

      val columns = Seq(imageCol, fileNameCol, nameCol, costValueCol, askPriceCol, importCol)

      def rowHandlerFor(item : InstagramItem) : Option[EventHandler] =  Some(((obj : JQueryEventObject) => {
        import dk.lokallykke.client.util.Modal
        import org.scalajs.dom

        val modalContents = List(
          Modal.EmbeddedImage(CommonUtil.toImageDataString(item.instagramResult.bytes, item.instagramResult.fileType)),
          Modal.EditableString("instagram-name", "Navn", item.name),
          Modal.EditableString("instagram-caption", "Beskrivelse", item.captionToUse),
          Modal.EditableDouble("instagram-costval", "Købsværdi", item.costValue),
          Modal.EditableDouble("instagram-askprice", "Til salg for", item.costValue)
        )
        Modal("instagram-modal", "Redigér genstand", modalContents, Some((ret) => {
          var updated = item
          ret.map(p => p._1 -> p._2()).foreach {
            case ("instagram-name", n : Option[String]) => updated = updated.copy(name = n)
            case ("instagram-caption", n : Option[String]) => updated = updated.copy(caption = n)
            case ("instagram-costval", d : Option[Double]) => updated = updated.copy(costValue = d)
            case ("instagram-askprice", d : Option[Double]) => updated = updated.copy(askPrice = d)
            case (itId,v) => {
              println(s"Found no way to update field: ${itId} with value: $v")
            }
          }
          instagramItems = instagramItems.map(en => if(en.instagramId == item.instagramId) updated else en)
          show()
        }))
      }))


      val tableBuilder = TableBuilder[InstagramItem]("item-instagram-item", columns, inRowHandler = Some(rowHandlerFor), imageSize = 40, inTableClass = "table table-hover table-sm")

      def show() : Unit = {
        clearContent()
        val tab = tableBuilder.buildTable(instagramItems)
        $(s"#$ItemsContentSubId").append(tab)
      }


    }

  }

  @JSExport
  def main(): Unit = {
    Items.ItemsConnector.connectToServer
    $(ItemsNavQuery).foreach((el : Element) => {
      $(el).click((obj : JQueryEventObject) => updateItemsNavSelection($(el).attr("id").toString))
    })
  }

  @JSExport
  def updateTable(itemsString : String): Unit = {
    implicit val decoder : Decoder[ViewItem] = deriveDecoder
    import io.circe.parser._
    parse(itemsString) match {
      case Left(err) => println(err.message)
      case Right(js) => {
        val items = js.asArray.get.map{i =>
          decode[ViewItem](i.toString).toOption.get
        }
        insertTable(items)
      }
    }
  }

  private def clearContent() = {
    $(s"#$ItemsContentId").empty()
    $(s"#$ItemsContentSubId").empty()
  }


  def updateItemsNavSelection(selected : String) : Unit = {
    $("[typ='items-nav']").removeAttr("aria-current").removeClass("active")
    $(s"#$selected").attr("aria-current", "page").addClass("active")
    clearContent()
    uploadedFiles = Nil

    selected match {
      case ItemsFromInstagramId => {
        ItemsConnector.send(ToServerMessage(ToServer.LoadInstagramItems))
        prepareForInstagramStatus()
      }
      case ItemsNavUploadId => {
        insertFileUpload()
      }
      case ItemsNavOnStockId => {
        ItemsConnector.requestItems()
      }
      case _ => {
        println(s"I have no action for: $selected")
      }
    }
  }

  def insertTable(items : Seq[ViewItem]) : Unit = {
    clearContent()
    val table = Tables.ItemTable.tableBuilder.buildTable(items)
    $(s"#$ItemsContentId").append(table)
  }

  def insertFileUpload() : Unit = {
    val upload = $("<form class='upload-box' method='post' action='' enctype='multipart/form-data' id='items-upload-box'>").append(
      $("<div class='items-upload-div m-4'>").append(
        $("<svg class='upload-icon' width='50' height='43' viewBox='0 0 50 43'>").append(
          $("<path d='M48.4 26.5c-.9 0-1.7.7-1.7 1.7v11.6h-43.3v-11.6c0-.9-.7-1.7-1.7-1.7s-1.7.7-1.7 1.7v13.2c0 .9.7 1.7 1.7 1.7h46.7c.9 0 1.7-.7 1.7-1.7v-13.2c0-1-.7-1.7-1.7-1.7zm-24.5 6.1c.3.3.8.5 1.2.5.4 0 .9-.2 1.2-.5l10-11.6c.7-.7.7-1.7 0-2.4s-1.7-.7-2.4 0l-7.1 8.3v-25.3c0-.9-.7-1.7-1.7-1.7s-1.7.7-1.7 1.7v25.3l-7.1-8.3c-.7-.7-1.7-.7-2.4 0s-.7 1.7 0 2.4l10 11.6z'>")
        ),
        $("<input class='upload-input' type='file' name='files[]' id='items-files-input' multiple>"),
        $("<label for='items-files-input' class='upload-label'>").append(
          $("<span class='upload-span'>").text("Drag files to upload")
        )
      )
    )
    $(s"#$ItemsContentId").append(upload)
    val uploadBox = $(s"#$ItemsUploadBoxId")
    $(uploadBox).on("drag dragstart dragend dragover dragenter dragleave drop", (ev : JQueryEventObject) => {
      ev.preventDefault()
      ev.stopPropagation()
    }).on("dragover dragenter", (ev : JQueryEventObject) => {
      $(uploadBox).addClass("is-dragover")
    }).on("dragleave dragend drop", (ev : JQueryEventObject) => {
      $(uploadBox).removeClass("is-dragover")
    }).on("drop", (el : Element, ev : JQueryEventObject, a1 : Any, a2 : Any) => {
      val filesList = ev.originalEvent.dataTransfer.files
      val files = for(i <- 0 until filesList.length) yield filesList(i)
      uploadFiles(files)
    })

  }

  def uploadFiles(files : Seq[File]): Unit = {
    val formData = new FormData() //(uploadBox.get(0).get.asInstanceOf[HTMLFormElement])
    files.foreach {
      case file => {
        formData.append($(s"#$ItemsFileInputId").attr("name"), file)
      }
    }
    val settings = js.Dynamic.literal(
      `type` = "post",
      dataType = "json",
      data = formData,
      cache = false,
      contentType = false,
      processData = false,
      complete = (jq : JQueryXHR) => {
      },
      success = (data : js.Any, textStatus : String, jqXHR : JQueryXHR) => {
        Try {
          implicit val fileResultDecoder : Decoder[FileUploadResult] = deriveDecoder[FileUploadResult]
          implicit val viewItemDecoder : Decoder[ViewItem] = deriveDecoder[ViewItem]
          implicit val instagramItemDecoder : Decoder[InstagramResult] = deriveDecoder[InstagramResult]
          implicit val decoder : Decoder[ToClientMessage] = deriveDecoder[ToClientMessage]
          import io.circe.parser.parse
          val stringified = JSON.stringify(data)
          val parseResult = parse(stringified)
          println(s"Parse result: $parseResult")
          for(
            json <- parseResult.toOption;
            mess <- json.as[ToClientMessage];
            items <- mess.items;
            res <- mess.uploadResult
          )  {
            println(s"Will add uploaded files table")
            val itemsMap = items.map(en => en.itemId -> en).toMap
            uploadedFiles = uploadedFiles ++ res.filter(_.success).map(r => UploadedFile(r.id, r.fileName, itemsMap(r.id)))
            Tables.UploadedFileTable.show()
          }
        }
      }
    ).asInstanceOf[JQueryAjaxSettings]
    $.ajax(Locations.Items.upload, settings)
  }

  def prepareForInstagramStatus() = {
    clearContent()
    $(s"#$ItemsContentId").append(
      $(s"<div class='d-flex justify-content-center'>").append(
        $(s"<textarea class='form-control mx-3 my-5' id='${ItemsInstagramTextareaId}' rows='10' cols='5' disabled>")
      )
    )
  }

  def updateWithInstagramStatus(status : String) = {
    val currentValue = $(s"#$ItemsInstagramTextareaId").value()
    val currentString = currentValue match {
      case _ if currentValue == null => ""
      case dyn => dyn.asInstanceOf[String]
    }
    $(s"#$ItemsInstagramTextareaId").value(currentString + "\n\r" + status)
  }

  case class UploadedFile(itemid : Long, fileName : Option[String], viewItem : ViewItem)
  case class InstagramItem(instagramId : String, name : Option[String], caption : Option[String], costValue : Option[Double], askPrice : Option[Double], instagramResult : ToClient.InstagramResult) {
    def captionToUse = caption.orElse(instagramResult.caption)
  }



  object ItemsConnector extends WSConnector {
    import io.circe.generic.auto._
    import io.circe.syntax._
    import io.circe.parser._
    import dk.lokallykke.client.Messages.Items._

    def requestItems() : Unit = {
      val mess = ToServer.ToServerMessage(ToServer.RequestItems)
      send(mess)
    }



    def sendViewItemUpdate(item : ViewItem) : Unit = {
      val mess = ToServer.ToServerMessage(ToServer.UpdateItemAndLoad, Some(item))
      send(mess)
    }

    override def onJson = Some((js : Json) => {
      js.as[ToClient.ToClientMessage].foreach {
        case mess => {
          mess.items.foreach {
            case its => {
              insertTable(its)
            }
          }
          mess.instagramUpdate.foreach(status => updateWithInstagramStatus(status))
          mess.instagramResults.foreach {
            case res => {
              instagramItems = res.map(en => InstagramItem(en.instagramId, None, en.caption, None, None, en))
              Tables.InstagramTable.show()
            }
          }
          mess.uploadedInstagramItem.foreach {
            case id => {
              instagramItems = instagramItems.filter(_.instagramId != id)
              Tables.InstagramTable.show()
            }
          }
        }
      }
    })

    def send(mess : ToServer.ToServerMessage) : Unit = {
      val asJson : Json = mess.asJson
      super.!(asJson.toString())

    }

  }

}

package dk.lokallykke.client.items

import dk.lokallykke.client.Locations
import dk.lokallykke.client.viewmodel.items.ViewItem
import org.querki.jquery.{$, ElementDesc, EventHandler, JQueryEventObject}
import dk.lokallykke.client.util.JsExtensions._
import dk.lokallykke.client.util.WSConnector
import dk.lokallykke.client.util.tables.{Column, TableBuilder}
import io.circe.generic.semiauto.deriveDecoder
import io.circe.{Decoder, Json, JsonObject}
import org.scalajs.dom.{Element, MessageEvent}

import java.time.LocalDateTime
import scala.scalajs.js.Date
import scala.scalajs.js.annotation.{JSExport, JSExportTopLevel, JSImport}


@JSExportTopLevel("Items")
object Items {
  val ItemsContentId = "items-content"
  val ItemsNavQuery = "[typ='items-nav']"
  val ItemsNavUploadId = "items-nav-upload"
  val ItemsNavOnStockId = "items-nav-on-stock"
  val ItemsNavSoldId = "items-nav-sold"
  val ItemsNavAllId = "items-nav-all"


  object Tables {
    import Column._
    object ItemTable {
      val imageCol = ImageColumn[ViewItem]("image", "Billede", None, en => Some(Locations.Items.itemImage(en.itemId)))
      val nameCol = StringColumn[ViewItem]("name", "Navn",_.name)
      val captionCol = StringColumn[ViewItem]("caption", "Beskrivelse",_.caption)
      val registeredCol = DateTimeColumn[ViewItem]("registered", "Registreret", en => Some(en.registered.toDateTime))
      val costValueCol = DoubleColumn[ViewItem]("costvalue", "Købsværdi", en => en.costValue )
      val askPriceCol = DoubleColumn[ViewItem]("askprice", "Til salg for", en => en.askPrice )
      val columns = Seq(imageCol, nameCol, captionCol, registeredCol, costValueCol, askPriceCol)

      def rowHandlerFor(item : ViewItem) : Option[EventHandler] =  Some(((obj : JQueryEventObject) => {
        import dk.lokallykke.client.util.Modal
        import Modal._
        import org.scalajs.dom

        val modalContents = List(
          Modal.Image("item-image",Locations.Items.itemImage(item.itemId)),
          Modal.EditableString("item-name", "Navn", item.name),
          Modal.EditableString("item-caption", "Beskrivelse", item.caption),
          Modal.EditableDouble("item-costval", "Købsværdi", item.costValue),
          Modal.EditableDouble("item-askprice", "Til salg for", item.askPrice)
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

  }


  def updateItemsNavSelection(selected : String) : Unit = {
    $("[typ='items-nav']").removeAttr("aria-current").removeClass("active")
    $(s"#$selected").attr("aria-current", "page").addClass("active")
    clearContent()

    selected match {
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
      $("<div class='upload-div'>").append(
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
      val mess = ToServer.ToServerMessage(ToServer.UpdateItem, Some(item))
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
        }
      }
    })

    private def send(mess : ToServer.ToServerMessage) : Unit = {
      val asJson : Json = mess.asJson
      super.!(asJson.toString())

    }

  }

}

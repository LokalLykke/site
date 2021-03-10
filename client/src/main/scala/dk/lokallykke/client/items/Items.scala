package dk.lokallykke.client.items

import dk.lokallykke.client.Locations
import dk.lokallykke.client.viewmodel.items.ViewItem
import org.querki.jquery.{$, ElementDesc, EventHandler, JQueryEventObject}
import dk.lokallykke.client.util.JsExtensions._
import dk.lokallykke.client.util.WSConnector
import dk.lokallykke.client.util.tables.{Column, TableBuilder}
import io.circe.generic.semiauto.deriveDecoder
import io.circe.{Decoder, Json, JsonObject}
import org.scalajs.dom.MessageEvent

import java.time.LocalDateTime
import scala.scalajs.js.Date
import scala.scalajs.js.annotation.{JSExport, JSExportTopLevel, JSImport}
@JSExportTopLevel("Items")
object Items {
  val ItemsTableInsert = "#items-table-insert"

  object Tables {
    import Column._
    object ItemTable {
      val imageCol = ImageColumn[ViewItem]("image", "Billede", None, en => Some(Locations.Items.itemImage(en.itemId)))
      val captionCol = StringColumn[ViewItem]("caption", "Beskrivelse",_.caption)
      val registeredCol = DateTimeColumn[ViewItem]("registered", "Registreret", en => Some(en.registered.toDateTime))
      val costValueCol = DoubleColumn[ViewItem]("costvalue", "Købsværdi", en => en.costValue )
      val askPriceCol = DoubleColumn[ViewItem]("askprice", "Til salg for", en => en.askPrice )
      val columns = Seq(imageCol, captionCol, registeredCol, costValueCol, askPriceCol)

      def rowHandlerFor(item : ViewItem) : Option[EventHandler] =  Some(((obj : JQueryEventObject) => {
        import dk.lokallykke.client.util.Modal
        import Modal._
        import org.scalajs.dom
        val modalContents = List(
          Modal.Image("item-image",Locations.Items.itemImage(item.itemId)),
          Modal.DisplayParagraph("item-description", "This is just a sample text to ensure that everything works as expected."),
          //Modal.EditableDateTime("item-registered", "Registreret", Some(LocalDateTime.now())),
          //Modal.EditableDate("item-date", "En dato eller sådan", Some(new Date(Date.now))),
          Modal.EditableDouble("item-costval", "Købsværdi", Some(100.23)),
          Modal.EditableString("item-caption", "Beskrivelse", Some("Holy hep"))
        )
        val changedValues = Modal("item-modal", "Redigér genstand", modalContents)
      }))

      val tableBuilder = TableBuilder[ViewItem]("item-table", columns, inRowHandler = Some(rowHandlerFor))


    }
  }

  @JSExport
  def main(): Unit = {
    Items.ItemsConnector.connectToServer
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
        val table = Tables.ItemTable.tableBuilder.buildTable(items)
        $(ItemsTableInsert).empty()
        $(ItemsTableInsert).append(table)
        //updateItemTable(items)
      }
    }
  }

  @JSExport
  def updateItemTable(items : Seq[ViewItem]) = {
    val bodyItems = items.map {
      case it => {
        val tr = $("<tr scope='row'>")
        val image = $("<td>").append($("<div>").append(
          $(s"<img src='${Locations.Items.itemImage(it.itemId)}' height='50' width='50' class='item-image'>"),
          $("<a href='#'>")
        ))
        val caption = $("<td>")text(it.caption.getOrElse(""))
        val registered = $("<td>").text(it.registered.toDateTime.toDateTimeString)
        val costvalue = $("<td>").text(it.costValue.map(v => v.toPrettyString).getOrElse(""))
        $(tr).append(image, caption, registered, costvalue)
        tr
      }
    }
    val body = $("<tbody>")
    bodyItems.foreach(it => $(body).append(it))


    val table =
      $("<table id='items-table' class='table table-hover'>").append(
        $("<thead>").append(
          $("<tr>").append(
            $("<th scope='col'>").text("Billede"),
            $("<th scope='col'>").text("Beskrivelse"),
            $("<th scope='col'>").text("Registreret"),
            $("<th scope='col'>").text("Købsværdi")
          )
        ),
        body
      )

    $(ItemsTableInsert).empty()
    $(ItemsTableInsert).append(table)
  }

  object ItemsConnector extends WSConnector {
    import io.circe.generic.auto._
    import io.circe.syntax._
    import io.circe.parser._
    import dk.lokallykke.client.Messages.Items._

    override def onJson = Some((js : Json) => {
      js.as[ToClient.RefreshItems].foreach {
        case refr => {
          updateItemTable(refr.items)
        }
      }
    })

  }

}

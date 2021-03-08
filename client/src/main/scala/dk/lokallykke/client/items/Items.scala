package dk.lokallykke.client.items

import dk.lokallykke.client.Locations
import dk.lokallykke.client.viewmodel.items.ViewItem
import org.querki.jquery.{$, ElementDesc}
import dk.lokallykke.client.util.JsExtensions._
import dk.lokallykke.client.util.WSConnector
import io.circe.generic.semiauto.deriveDecoder
import io.circe.{Decoder, Json, JsonObject}
import org.scalajs.dom.MessageEvent

import scala.scalajs.js.annotation.{JSExport, JSExportTopLevel}

@JSExportTopLevel("Items")
object Items {
  val ItemsTableInsert = "#items-table-insert"

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
          println(s"Item: ${i} of type : ${i.getClass.toString}")
          decode[ViewItem](i.toString).toOption.get
        }
        updateItemTable(items)
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

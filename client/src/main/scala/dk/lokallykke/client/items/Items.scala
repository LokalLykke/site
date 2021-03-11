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
        updateTable(items)
      }
    }
  }

  def updateTable(items : Seq[ViewItem]) : Unit = {
    val table = Tables.ItemTable.tableBuilder.buildTable(items)
    $(ItemsTableInsert).empty()
    $(ItemsTableInsert).append(table)
  }

  object ItemsConnector extends WSConnector {
    import io.circe.generic.auto._
    import io.circe.syntax._
    import io.circe.parser._
    import dk.lokallykke.client.Messages.Items._

    def sendViewItemUpdate(item : ViewItem) : Unit = {
      val mess = ToServer.ToServerMessage(ToServer.UpdateItem, Some(item))
      val asJson : Json = mess.asJson
      super.!(asJson.toString())
    }

    override def onJson = Some((js : Json) => {
      js.as[ToClient.ToClientMessage].foreach {
        case mess => {
          mess.items.foreach {
            case its => {
              updateTable(its)
            }
          }
        }
      }
    })

  }

}

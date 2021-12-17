package dk.lokallykke.client.customerpages

import dk.lokallykke.client.util.{Modal, WSConnector}
import dk.lokallykke.client.viewmodel.customer.CustomerItem
import dk.lokallykke.client.util.JQueryExtensions._
import org.querki.jquery
import org.querki.jquery.$

import scala.scalajs.js.annotation.{JSExport, JSExportTopLevel}

@JSExportTopLevel("CustomerPages")
object CustomerPages {
  private val ItemImagesPerRow = 4

  private var items  = Seq.empty[CustomerItem]

  @JSExport
  def setItems(itemsString : String) : Unit = {
    import io.circe.Decoder
    import io.circe.generic.semiauto.deriveDecoder
    implicit val decoder : Decoder[CustomerItem] = deriveDecoder
    import io.circe.parser._
    parse(itemsString) match {
      case Left(err) => println(err.message)
      case Right(js) => {
        val parsedItems = js.asArray.get.map{i =>
          decode[CustomerItem](i.toString).toOption.get
        }
        items = parsedItems
        $("#" + Elements.ImageGridHolderId).empty()
        val grid = buildImageGrid
        $("#" + Elements.ImageGridHolderId).append(grid)
      }
    }
  }

  def bindImageCard(item : CustomerItem, elem : jquery.ElementDesc) : Unit = {
    $(elem).click(() => {
      val modalContents = List(
        Modal.Image("modal-item-image-" + item.id, item.imageUrl)
      )
      Modal("item-modal", item.name, modalContents, None, showFooter = false, modalClass = "modal-dialog modal-xl modal-dialog-centered")
    })
  }



  def buildImageGrid = {
    val cards : Seq[jquery.ElementDesc] = items.map {
      case itm => {
        val element = $("<div class='col'>").append(
          $("<div class='card mb-3'>").append(
            $(s"<img src='${itm.imageUrl}' class='card-img-top rounded'>").append(
              $("<div class='card-body'>").append(
                $("<h5 class='card-title'>").text(itm.name),
                $("<p class='card-text'>").text(itm.caption.getOrElse(""))
              )
            )
          )
        )
        bindImageCard(itm, element)
        element
      }
   }
    $(s"<div class='row row-cols-${ItemImagesPerRow}'>").append(
      cards : _*
    )
  }

  object CustomerPagesConnector extends WSConnector {
    import io.circe.generic.auto._
    import io.circe.syntax._
    import io.circe.parser._
    import dk.lokallykke.client.Messages.Items._



  }


}

package dk.lokallykke.client.util

import org.querki.jquery.{$, JQuery, JQueryStatic}

import java.time.LocalDateTime
import scala.scalajs.js
import scala.scalajs.js.Date
import scala.scalajs.js.annotation.JSImport

object Modal {

  val ModalDivId = "main-modal-holder"

  private object BootstrapLib {
    @js.native
    @JSImport("bootstrap", JSImport.Namespace)
    object BootstrapModule extends js.Object

    private lazy val dummy = BootstrapModule

    def load() = dummy
  }
  BootstrapLib.load()

  @js.native
  trait BootstrapJQuery extends JQuery {
    def modal(action: String): BootstrapJQuery = js.native
    def modal(options: js.Any): BootstrapJQuery = js.native
  }

  implicit def jq2bootstrap(jq: JQuery): BootstrapJQuery = jq.asInstanceOf[BootstrapJQuery]


  def apply(id : String, title : String, fields : Seq[ModalField]) : Map[String, Any] = {
    val body = $("<div class='modal-body'>")
    val contents = fields.map(bodyContentFromField)
    contents.foreach(c => $(body).append(c))


    val modal = $(s"<div class='modal fade' id='$id' role='dialog' aria-labelledby='$id-center-title' aria-hidden='true'>").append(
      $("<div class='modal-dialog modal-dialog-centered' role='document'>").append(
        $("<div class='modal-content'>").append(
          $("<div class='modal-header'>").append(
            $(s"<h5 class='modal-title' id='$id-center-title'>").text(title),
            $("<button type='button' class='close' data-dismiss='modal' aria-label='Close'>").append(
              $("<span aria-hidden='true'>").html("&times;")
            )
          ),
          body
        )
      )
    )
    displayModal(modal)
    Map.empty
  }

  def bodyContentFromField(field : ModalField) = {

    val content = field match {
      case DisplayParagraph(id,text) => $(s"<p id='$id'>").html(text)
      case Image(id, url) => $(s"<img src='$url' class='img-fluid'>")
      case _ => $("")
    }
    content
  }

  def displayModal(modal : JQuery)  = {
    $(s"#$ModalDivId").empty()
    $(s"#$ModalDivId").append(modal)
    modal.modal("show")
  }

  sealed trait ModalField
  case class EditableString(id : String, key : String, value : Option[String]) extends ModalField
  case class EditableDouble(id : String, key : String, value : Option[Double]) extends ModalField
  case class EditableDate(id : String, key : String, value : Option[Date]) extends ModalField
  case class EditableDateTime(id : String, key : String, value : Option[LocalDateTime]) extends ModalField
  case class DisplayParagraph(id : String, text : String) extends ModalField
  case class Image(id : String, url : String) extends ModalField




}

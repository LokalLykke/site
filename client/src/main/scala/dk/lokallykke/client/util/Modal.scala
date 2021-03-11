package dk.lokallykke.client.util

import org.querki.jquery.{$, JQuery, JQueryEventObject, JQueryStatic}

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

  type ValueResolver =  () => Option[Any]


  def apply(id : String, title : String, fields : Seq[ModalField], onSave : Option[(Map[String, ValueResolver]) => Unit] = None) : Unit = {
    val body = $("<div class='modal-body'>")
    val resolvers = scala.collection.mutable.ArrayBuffer.empty[(String, () => Option[Any])]
    val contents = fields.map(bodyContentFromField)
    contents.foreach{
      case (cont, valRes) => {
        $(body).append(cont)
        valRes.foreach(vr => resolvers += vr)
      }
    }

    val saveButton = $(s"<button id='$id-save-button' type='button' class='btn btn-primary'>").text("Gem")
    $(saveButton).click((ev : JQueryEventObject) => {
      val values = resolvers.toMap
      onSave.foreach(evh => evh(values))
      $(s"#$id").modal("hide")
    })

    val footer =  $("<div class='modal-footer'>").append(
      $(s"<button id='$id-cancel-button' type='button' class='btn btn-secondary' data-dismiss='modal'>").text("Annullér"),
      saveButton
      )


    val modal = $(s"<div class='modal fade' id='$id' role='dialog' aria-labelledby='$id-center-title' aria-hidden='true'>").append(
      $("<div class='modal-dialog modal-dialog-centered' role='document'>").append(
        $("<div class='modal-content'>").append(
          $("<div class='modal-header'>").append(
            $(s"<h5 class='modal-title' id='$id-center-title'>").text(title),
            $("<button type='button' class='close' data-dismiss='modal' aria-label='Close'>").append(
              $("<span aria-hidden='true'>").html("&times;")
            )
          ),
          body,
          footer
        )
      )
    )
    displayModal(modal)
  }

  def bodyContentFromField(field : ModalField) : (JQuery, Option[(String, () => Option[Any])]) = {
    import JsExtensions._
    val ret = field match {
      case DisplayParagraph(id,text) => ($(s"<p id='$id'>").html(text), None.asInstanceOf[Option[(String,() => Option[Any])]])
      case Image(id, url) => ($(s"<img src='$url' class='img-fluid'>"),None.asInstanceOf[Option[(String, () => Option[Any])]])
      case EditableDateTime(id, key, value) => {
        val input = $("<div class='my-2'>").append(
          $(s"<label for='$id' class='form-label'>").text(key),
          $(s"<input id='$id' type='datetime-local' class='form-control' value='${value.map(_.toInputDateString).getOrElse("")}'>")
        )
        var retVal : Option[LocalDateTime] = None
        $(input).change((ev : JQueryEventObject) => {
          val newVal = $(s"#$id").value()
          retVal = if(newVal != null) newVal.toString.toDateTimeFromInput else None
          println(s"New datetime value: ${retVal} of type : ${newVal.getClass}")
        })
        val valRes = Some((id, () => retVal))
        (input, valRes)
      }
      case EditableDate(id, key, value) => {
        val input = $("<div class='my-2'>").append(
          $(s"<label for='$id' class='form-label'>").text(key),
          $(s"<input id='$id' type='date' class='form-control' value='${value.map(_.toInputDateString).getOrElse("")}'>")
        )
        var retVal : Option[Date] = None
        $(input).change((ev : JQueryEventObject) => {
          val newVal = $(s"#$id").value()
          retVal = if(newVal != null) newVal.toString.toDateFromInput else None
          println(s"New date value: ${retVal} of type : ${newVal.getClass}")
        })
        val valRes = Some((id, () => retVal))
        (input, valRes)

      }
      case EditableDouble(id, key, value) => {
        val input = $("<div class='my-2'>").append(
          $(s"<label for='$id' class='form-label'>").text(key),
          $(s"<input id='$id' type='number' class='form-control' lang='da-DK' step='0.01' value='${value.map(_.toInputString).getOrElse("")}'>")
        )

        var retVal : Option[Double] = None
        $(input).change((ev : JQueryEventObject) => {
          val newVal = $(s"#$id").value()
          retVal = if(newVal != null && newVal.toString.size > 0) newVal.toString.toDoubleFromInput else None
          println(s"New double value: ${retVal} of type : ${newVal.getClass}")
        })
        val valRes = Some((id, () => retVal))
        (input, valRes)
      }
      case EditableString(id, key, value) => {
        val input = $("<div class='my-2'>").append(
          $(s"<label for='$id' class='form-label'>").text(key),
          $(s"<input id='$id' type='text' class='form-control' value='${value.getOrElse("")}'>")
        )
        var retVal : Option[String] = None
        $(input).change((ev : JQueryEventObject) => {
          val newVal = $(s"#$id").value()
          if(newVal != null && newVal.toString.length > 0)
            retVal = Some(newVal.toString)
          else retVal = None

          println(s"New string value: ${retVal} of type : ${newVal.getClass}")
        })
        val valRes = Some((id, () => retVal))
        (input, valRes)
      }

      case _ => ($(""),None.asInstanceOf[Option[(String, () => Option[Any])]])
    }
    ret
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
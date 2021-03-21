package dk.lokallykke.client.util

import dk.lokallykke.client.util.CommonUtil.$i
import dk.lokallykke.client.util.tables.{Column, TableBuilder}
import org.querki.jquery.{$, JQuery, JQueryEventObject, JQueryStatic}

import java.time.LocalDateTime
import scala.scalajs.js
import scala.scalajs.js.Date
import scala.scalajs.js.annotation.JSImport

object Modal {
  import dk.lokallykke.client.util.JQueryExtensions._

  val ModalDivId = "main-modal-holder"


  type ValueResolver =  () => Option[Any]


  def apply(id : String, title : String, fields : Seq[ModalField], onSave : Option[(Map[String, ValueResolver]) => Unit] = None, saveText : String = "Gem", cancelText : String = "Annuller") : Unit = {
    val body = $("<div class='modal-body'>")
    val resolvers = scala.collection.mutable.ArrayBuffer.empty[(String, () => Option[Any])]
    val contents = fields.filter(en => if(en.isInstanceOf[SelectableOptions]) false else true).map(bodyContentFromField)
    contents.foreach{
      case (cont, valRes) => {
        $(body).append(cont)
        valRes.foreach(vr => resolvers += vr)
      }
    }

    val saveButton = $(s"<button id='$id-save-button' type='button' class='btn btn-primary'>").text(saveText)
    $(saveButton).click((ev : JQueryEventObject) => {
      val values = resolvers.toMap
      onSave.foreach(evh => evh(values))
      $(s"#$id").modal("hide")
    })

    val footer =  $("<div class='modal-footer'>").append(
      $(s"<button id='$id-cancel-button' type='button' class='btn btn-secondary' data-dismiss='modal'>").text(cancelText),
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
    fields.collect {
      case selOpt : SelectableOptions => {
        val selRes = insertSelector(body, selOpt)
        selRes._2.foreach(vr => resolvers += vr)
      }
    }
  }

  def tabled[A](id : String, title : String, cols : Seq[Column[A,_]], items : Seq[A]) : Unit = {
    val body = $(s"<div class='modal-body' id='$id-table-insert'>")
    val tableBuilder = TableBuilder[A](s"$id-table", cols, imageSize = 20, inTableClass = "table table-hover table-sm")
    val tab = tableBuilder.buildTable(items)
    body.append(
      $(tab)
    )

    val modal = $(s"<div class='modal fade' id='$id' role='dialog' aria-labelledby='$id-center-title' aria-hidden='true'>").append(
      $("<div class='modal-dialog modal-dialog-centered modal-xl' role='document'>").append(
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
  }

  def bodyContentFromField(field : ModalField) : (JQuery, Option[(String, () => Option[Any])]) = {
    import JsExtensions._
    val ret = field match {
      case DisplayParagraph(id,text) => ($(s"<p id='$id'>").html(text), None.asInstanceOf[Option[(String,() => Option[Any])]])
      case Image(id, url) => ($(s"<img src='$url' class='img-fluid'>"),None.asInstanceOf[Option[(String, () => Option[Any])]])
      case EmbeddedImage(dataString) => ($(s"<img src='$dataString' class='img-fluid'>"),None.asInstanceOf[Option[(String, () => Option[Any])]])
      case EditableDateTime(id, key, value) => {
        val input = $("<div class='my-2'>").append(
          $(s"<label for='$id' class='form-label'>").text(key),
          $(s"<input id='$id' type='datetime-local' class='form-control' value='${value.map(_.toInputDateString).getOrElse("")}'>")
        )
        var retVal : Option[LocalDateTime] = value
        $(input).change((ev : JQueryEventObject) => {
          val newVal = $(s"#$id").value()
          retVal = if(newVal != null) newVal.toString.toDateTimeFromInput else None
        })
        val valRes = Some((id, () => retVal))
        (input, valRes)
      }
      case EditableDate(id, key, value) => {
        val input = $("<div class='my-2'>").append(
          $(s"<label for='$id' class='form-label'>").text(key),
          $(s"<input id='$id' type='date' class='form-control' value='${value.map(_.toInputDateString).getOrElse("")}'>")
        )
        var retVal : Option[Date] = value
        $(input).change((ev : JQueryEventObject) => {
          val newVal = $(s"#$id").value()
          retVal = if(newVal != null) newVal.toString.toDateFromInput else None
        })
        val valRes = Some((id, () => retVal))
        (input, valRes)

      }
      case EditableDouble(id, key, value) => {
        val input = $("<div class='my-2'>").append(
          $(s"<label for='$id' class='form-label'>").text(key),
          $(s"<input id='$id' type='number' class='form-control' lang='da-DK' step='0.01' value='${value.map(_.toInputString).getOrElse("")}'>")
        )

        var retVal : Option[Double] = value
        $(input).change((ev : JQueryEventObject) => {
          val newVal = $(s"#$id").value()
          retVal = if(newVal != null && newVal.toString.size > 0) newVal.toString.toDoubleFromInput else None
        })
        val valRes = Some((id, () => retVal))
        (input, valRes)
      }
      case EditableString(id, key, value) => {
        val input = $("<div class='my-2'>").append(
          $(s"<label for='$id' class='form-label'>").text(key),
          $(s"<input id='$id' type='text' class='form-control' value='${value.getOrElse("")}'>")
        )
        var retVal : Option[String] = value
        $(input).change((ev : JQueryEventObject) => {
          val newVal = $(s"#$id").value()
          if(newVal != null && newVal.toString.length > 0)
            retVal = Some(newVal.toString)
          else retVal = None
        })
        val valRes = Some((id, () => retVal))
        (input, valRes)
      }
      case _ => ($(""),None.asInstanceOf[Option[(String, () => Option[Any])]])
    }
    ret
  }

  def insertSelector(body : JQuery, selOpt : SelectableOptions) = {
    val selSet = selOpt.selected.toSeq.flatten.toSet
    val optSet = selOpt.options.toSet
    val allOpts = selOpt.options ++ selOpt.selected.toSeq.flatten.filter(s => !optSet(s))
    val input = $("<div class='my-2'>").append(
      $(s"<label for='${selOpt.id}' class='form-label'>").text(selOpt.key)
    )
    $(body).append(input)
    val select = Selector(selOpt.id, body, allOpts, selOpt.selected.toList.flatten)
    var retVal : Option[Seq[String]] = selOpt.selected
    import Selector._
    $(select).change((ev : JQueryEventObject) => {
      $(s"#${selOpt.id}").value() match {
        case null => println(s"selectize element (${selOpt.id}) value was null")
        case str if str.isInstanceOf[String] => {
          retVal = if(str.asInstanceOf[String].length > 0) Some(str.asInstanceOf[String].split(";").toSeq) else None
        }
        case othr => println(s"Got back ${othr} of class: ${othr.getClass} as selectize element (${selOpt.id}) value")
      }
    })
    val valRes = Some(selOpt.id, () => retVal)
    (input, valRes)

  }

  def displayModal(modal : JQuery)  = {
    $(s"#$ModalDivId").empty()
    $(s"#$ModalDivId").append(modal)
    modal.modal("show")
  }

  def Accept(title : String, text : String, acceptHandler : () => Any, acceptText : String = "Ok", cancelText : String = "Annuller") = {
    Modal("builtin-accept-modal", title, List(DisplayParagraph("builtin-accept-text", text)), Some( (in : Map[String, ValueResolver]) => {
      acceptHandler()
    }), saveText = acceptText, cancelText = cancelText)
  }

  sealed trait ModalField
  case class EditableString(id : String, key : String, value : Option[String]) extends ModalField
  case class EditableDouble(id : String, key : String, value : Option[Double]) extends ModalField
  case class EditableDate(id : String, key : String, value : Option[Date]) extends ModalField
  case class EditableDateTime(id : String, key : String, value : Option[LocalDateTime]) extends ModalField
  case class DisplayParagraph(id : String, text : String) extends ModalField
  case class Image(id : String, url : String) extends ModalField
  case class EmbeddedImage(dataString : String) extends ModalField
  case class SelectableOptions(id : String, key : String, options : Seq[String], selected : Option[Seq[String]]) extends ModalField




}

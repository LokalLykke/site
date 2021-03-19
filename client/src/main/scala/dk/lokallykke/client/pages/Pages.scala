package dk.lokallykke.client.pages

import dk.lokallykke.client.util.CommonUtil.$i
import dk.lokallykke.client.util.{Selector, Validation}
import dk.lokallykke.client.util.editor.Editor
import dk.lokallykke.client.util.editor.Editor.OutputDataParser
import dk.lokallykke.client.util.FutureExtensions._
import dk.lokallykke.client.viewmodel.pages.ViewPage
import org.querki.jquery.{$, JQuery, JQueryEventObject}

import scala.scalajs.js
import js.Thenable.Implicits._
import scala.concurrent.ExecutionContext
import scala.scalajs.js.UndefOr
import scala.scalajs.js.annotation.{JSExport, JSExportTopLevel}

@JSExportTopLevel("Pages")
object Pages {
  val ContentId = "pages-content"
  val FormId = "pages-form"
  val FormNameId = "pages-form-name"
  val FormDescriptionId = "pages-form-description"
  val FormTagsHolderId = "pages-form-tags-insert"
  val FormTagsId = "pages-form-tags"
  val FormButtonId = "pages-form-button"
  val BlockEditorId = "pages-blocks-editor"


  var allTags : Seq[String] = Nil
  var tagSelector : Option[typings.selectize.JQuery] = None


  @JSExport
  def main() : Unit = {

    val editor = Editor(BlockEditorId)
    $i(FormButtonId).click((obj : JQueryEventObject) => {
      Validation.validateAndPerform(List(FormNameId), () => {
        editor.save().toFuture.whenDone {
          case outData => {
            outData.blocks.foreach {
              case block => println(block)
            }
          }
        }
      })
      val parser = OutputDataParser(editor.save())
    })
  }

  @JSExport
  def setTags(tags : String) : Unit = {
    allTags = tags.split(";")
  }

  def insertViewPage(page : ViewPage) : Unit = {
    $i(ContentId).empty()
    $i(ContentId).append(
      $("<div class='row'>").append(
        $("<div class='col-12'>").append(
          $(s"<form id='$FormId' class='border my-4 p-4 bg-light'>").append(
            $("<div class='form-row'>").append(
              $("<div class='form-group col-6'>").append(
                $(s"<label for='$FormNameId'>").text("Side-navn"),
                $(s"<input type='text' id='$FormNameId' class='form-control' placeholder='Navn' required>").value(page.name)
              ),
              $("<div class='form-group col-6'>").append(
                $(s"<label for='$FormDescriptionId'>").text("Beskrivelse"),
                $(s"<textarea type='text' id='$FormDescriptionId' class='form-control' placeholder='Navn' rows='3'>").text(page.description.getOrElse(""))
              )
            ),
            $("<div class='form-row'>").append(
              $(s"<div class='form-group col-12' id='$FormTagsHolderId'>").append(
                $(s"<label for='$FormTagsId'>").text("Mærkater"),
                $(s"<input id='$FormTagsId'>")
              )
            ),
            $("<div class='form-row justify-content-end'>").append(
              $("<div class='form-group col'>").append(
                $(s"<button type='button' id='$FormButtonId' class='btn btn-info'>").text("Gem")
              )
            )
          )
        )
      ),
      $("<div class='border p-5 bg-light'>").append(
        $(s"<div id='pages-blocks-editor'>")
      )
    )
    tagSelector = Some(Selector(FormTagsId, appendTo = $(s"#$FormTagsHolderId"), allTags, Nil))
  }




}

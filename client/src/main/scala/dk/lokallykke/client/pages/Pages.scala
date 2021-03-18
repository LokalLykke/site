package dk.lokallykke.client.pages

import dk.lokallykke.client.util.Selector
import dk.lokallykke.client.util.editor.Editor
import dk.lokallykke.client.util.editor.Editor.OutputDataParser
import org.querki.jquery.{$, JQuery, JQueryEventObject}

import scala.scalajs.js
import js.Thenable.Implicits._
import scala.concurrent.ExecutionContext
import scala.scalajs.js.UndefOr
import scala.scalajs.js.annotation.{JSExport, JSExportTopLevel}

@JSExportTopLevel("Pages")
object Pages {
  val FormTagsHolderId = "pages-form-tags-insert"
  val FormTagsId = "pages-form-tags"
  val FormButtonId = "pages-form-button"
  val BlockEditorId = "pages-blocks-editor"

  var allTags : Seq[String] = Nil
  var tagSelector : Option[typings.selectize.JQuery] = None


  @JSExport
  def main() : Unit = {

    val editor = Editor(BlockEditorId)
    $(s"#$FormButtonId").click((obj : JQueryEventObject) => {
      implicit val ec = ExecutionContext.global
      val parser = OutputDataParser(editor.save())
      parser.result.onComplete {
        case resTry => {
          resTry.toOption.foreach {
            case blocks => {
              blocks.foreach {
                case block => {
                  println(block)
                }
              }
            }
          }
        }
      }
    })
  }

  @JSExport
  def setTags(tags : String) : Unit = {
    allTags = tags.split(";")
    $(s"#$FormTagsId").remove()
    tagSelector = Some(Selector(FormTagsId, appendTo = $(s"#$FormTagsHolderId"), allTags, Nil))
  }


}

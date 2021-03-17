package dk.lokallykke.client.pages

import dk.lokallykke.client.util.Selector
import dk.lokallykke.client.util.editor.Editor
import org.querki.jquery.{$, JQueryEventObject}

import scala.scalajs.js
import js.Thenable.Implicits._
import scala.concurrent.ExecutionContext
import scala.scalajs.js.annotation.{JSExport, JSExportTopLevel}

@JSExportTopLevel("Pages")
object Pages {
  val FormTagsId = "pages-form-tags"
  val FormButtonId = "pages-form-button"
  var allTags : Seq[String] = Nil

  @JSExport
  def main() : Unit = {
    Selector("pages-form-tags", allTags, List("op"))
    val editor = Editor("crazy-editor")
    $(s"#$FormButtonId").click((obj : JQueryEventObject) => {
      val future = editor.save().toFuture
      implicit val ec = ExecutionContext.global
      future.onComplete((tr) => {
        println("Done finished saving")
      })
    })
  }

  @JSExport
  def setTags(tags : String) : Unit = {
    allTags = tags.split(";")
  }


}

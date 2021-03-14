package dk.lokallykke.client.util

import org.querki.jquery.{$, JQuery}
import typings.selectize.Selectize.IOptions

import scala.scalajs.js
import scala.scalajs.js.annotation.JSImport
import dk.lokallykke.client.util.JsExtensions._

// Documentation: https://github.com/selectize/selectize.js/blob/master/docs/api.md
object Selector {

  @js.native
  @JSImport("selectize/dist/css/selectize.default.css", JSImport.Namespace)
  object Css extends js.Object

  object SelectizeLib {
    private val reqSelectize = typings.selectize.selectizeRequire

    //@js.native
    //@JSImport("@types/selectize", JSImport.Namespace)
    object Dependency extends js.Object

    private lazy val resolves = Dependency
    def load() = resolves
  }
  SelectizeLib.load()
  import typings.selectize.{JQuery => SelectizeJQuery}

  implicit def jQuerySelectizeExtender(jq : JQuery) : SelectizeJQuery = jq.asInstanceOf[SelectizeJQuery]

  val selectizeCss = Css

  def apply(id : String, appendTo : JQuery, options : Seq[String] = Nil, selected : Seq[String] = Nil) = {
    $(appendTo).append(
      $(s"<input id='$id'>")
    )
    val opts = IOptions[String, String]
      .setItems(selected.toArray.toJsArray)
      .setOptions(options.toArray.toJsArray)
      .setCreate(true)
      .setDelimiter(";")
    val ret = $(s"#$id").selectize(opts)
    ret
  }







}

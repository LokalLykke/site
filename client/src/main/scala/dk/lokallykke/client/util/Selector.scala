package dk.lokallykke.client.util

import org.querki.jquery.{$, JQuery}
import typings.selectize.Selectize.{IApi, IOptions}

import scala.scalajs.js
import scala.scalajs.js.annotation.JSImport
import dk.lokallykke.client.util.JsExtensions._
import org.scalablytyped.runtime.StringDictionary
import typings.selectize.{HTMLElement, Selectize}

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
  //implicit def jQuerySelectizaApi(jq : JQuery) : IApi[String, String] = jq.asInstanceOf[IApi[String, String]]

  val selectizeCss = Css

  def apply(id : String, appendTo : JQuery, options : Seq[String] = Nil, selected : Seq[String] = Nil) = {
    val input = $(s"<input id='$id'>")
    $(appendTo).append($(input))
    selected.foreach(sel => println(sel))

    val opts = IOptions[String, js.Object with js.Dynamic]
      .setCreate(true)
      .setValueField("id")
      .setLabelField("name")
      .setDelimiter(";")
      .setOptions(options.map(opt => js.Dynamic.literal(id = opt, name = opt)).toJsArray)
      .setItems(selected.toJsArray)

    val ret = $(s"#$id").selectize(opts)
    ret
  }

  def initialize(jqString : String, options : Seq[String] = Nil, selected : Option[Seq[String]] = None) : Unit = {
    val opts = IOptions[String, String]
      .setCreate(true)
      .setDelimiter(";")
      .setOptions(options.toArray.toJsArray)
    val withSelected = selected match {
      case Some(sels) => opts.setItems(sels.toArray.toJsArray)
      case _ => opts
    }
    println(s"Initializing by query string: $jqString finding: ${$(jqString)}")

    $(jqString).selectize(withSelected)

  }







}

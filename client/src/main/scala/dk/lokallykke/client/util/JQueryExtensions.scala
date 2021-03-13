package dk.lokallykke.client.util

import org.querki.jquery.{JQuery, JQueryEventObject}
import org.scalajs.dom.DragEvent

import scala.scalajs.js
import scala.scalajs.js.annotation.JSImport

object JQueryExtensions {

  object BootstrapLib {
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

  @js.native
  trait JQueryEventObjectExtensions extends JQueryEventObject {
    def originalEvent : DragEvent = js.native
  }

  implicit def jq2ext(jqo : JQueryEventObject) = jqo.asInstanceOf[JQueryEventObjectExtensions]


}

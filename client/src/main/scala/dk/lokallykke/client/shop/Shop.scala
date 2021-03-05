package dk.lokallykke.client.shop

import org.scalajs.dom.{Event, WebSocket}
import org.scalajs.dom.raw._

import scala.scalajs.js.annotation.{JSExport, _}
import io.circe._
import io.circe.parser._

@JSExportTopLevel("Shop", "main")
object Shop {

  @JSExport
  def main() : Unit = {
    println("Hello from the shop")
  }

}

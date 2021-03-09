package dk.lokallykke.client.util.tables

import org.querki.jquery.{$, EventHandler, JQuery}

import scala.scalajs.js.annotation.JSExportTopLevel

trait TableBuilder[A] {
  val id : String
  val columns : Seq[Column[A,_]]
  def buildTable(items : Seq[A], rowHandler : Option[A => Option[EventHandler]], isBold : Option[A => Boolean])
}



object TableBuilder {

  def apply[A](inId : String, inColumns : Seq[Column[A,_]], inRowHandler : Option[A => Option[EventHandler]] = None, inIsBold : Option[A => Boolean] = Some((a : A) => false)) = {

    new TableBuilder[A] {
      override val id: String = inId
      override val columns: Seq[Column[A, _]] = inColumns

      override def buildTable(items: Seq[A], rowHandler: Option[A => Option[EventHandler]], isBold: Option[A => Boolean]): Unit = {
      }
    }

    def createHeader = {
      val header = $("<thead>")
      val headerRow = $("<tr>")
      header.append(headerRow)

      inColumns.foreach {
        case col => headerRow.append(
          $(s"<th scope='col' column-id='${col.id}'>").text(col.name)
        )
      }
      header
    }

  }

  def buildTable[A](items : Seq[A]) : JQuery = {
    $("")
  }


}

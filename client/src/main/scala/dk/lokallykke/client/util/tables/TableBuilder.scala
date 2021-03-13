package dk.lokallykke.client.util.tables

import org.querki.jquery.{$, EventHandler, JQuery}

import scala.scalajs.js.annotation.JSExportTopLevel

trait TableBuilder[A] {
  val id : String
  val columns : Seq[Column[A,_]]
  def buildTable(items : Seq[A]) : JQuery
}



object TableBuilder {

  def apply[A](inId : String, inColumns : Seq[Column[A,_]], inRowHandler : Option[A => Option[EventHandler]] = None, inIsBold : Option[A => Boolean] = Some((a : A) => false), imageSize : Int = 75, inTableClass : String = "table table-hover") : TableBuilder[A]  = {
    new TableBuilder[A] {
      override val id: String = inId
      override val columns: Seq[Column[A, _]] = inColumns

      override def buildTable(items: Seq[A]): JQuery = {
        val header = createHeader
        val body = createBody(items)
        val table = $(s"<table id='$inId' class='$inTableClass'>").append(
          header,
          body
        )
        table
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

      def createBody(items: Seq[A]) = {
        val body = $("<tbody>")
        items.foreach {
          case item => {
            val row = $("<tr scope='row'>")
            inColumns.foreach {
              case col => {
                val td = $("<td>")
                col.imageUrl(item) match {
                  case Some(url) => {
                    $(td).append($("<div>").append(
                      $(s"<img src='$url' height='$imageSize' width='$imageSize' class='item-image'>"),
                      $("<a href='#'>")
                    ))
                  }
                  case None => {
                    val value = col.stringValue(item)
                    $(td).text(value)
                  }
                }
                col.onClick(item).foreach {
                  case cellHandler => {
                    $(td).click(cellHandler)
                  }
                }
                $(row).append(td)
              }
            }
            for (
              rowHandler <- inRowHandler;
              handler <- rowHandler(item)
            ) {
              $(row).click(handler)
            }
            if (inIsBold.map(_ (item)).getOrElse(false)) {
              $(row).attr("style", "font-weight: bold;")
            }
            $(body).append(row)
          }
        }
        body
      }

    }
  }


}

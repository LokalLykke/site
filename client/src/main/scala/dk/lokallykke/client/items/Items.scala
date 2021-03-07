package dk.lokallykke.client.items

import dk.lokallykke.client.Locations
import dk.lokallykke.client.viewmodel.items.ViewItem
import org.querki.jquery.$

object Items {

  def buildItemTable(items : Seq[ViewItem]) = {
    $("<table id='items-table' class='table table-hover'>").append(
      $("<thead>").append(
        $("<tr>").append(
          $("<th scope='col'>").text("Billede"),
          $("<th scope='col'>").text("Beskrivelse"),
          $("<th scope='col'>").text("Registreret"),
          $("<th scope='col'>").text("Købsværdi")
        )
      ),
      $("<tbody>").append(
        (items.map {
          case it => {
            val tr = $("<tr scope='row'>")
            val image = $("<td>").append($("<div>").append(
              $(s"<img src='${Locations.Items.itemImage(it.itemId)}' height='50' width='50' class='item-image'>"),
              $("<a href='#'>")
            ))
            val caption = $("<td>")text(it.caption.getOrElse(""))
            val registered = $("<td>").text(new scalajs.js.Date(it.registered.getTime.toDouble).formatted("dd-MM-yyyy"))
            val costvalue = $("<td>").text(it.costValue.map(v => v.toString).getOrElse(""))
            $(tr).append(image, caption, registered, costvalue)
            tr
          }
        }) : _ *
      )
    )
  }

}

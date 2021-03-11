package dk.lokallykke.client.viewmodel.items

import java.sql.Timestamp

case class ViewItem(
                     itemId : Long,
                     instaId : Option[Long],
                     name : Option[String],
                     caption : Option[String],
                     registered : Long,
                     costValue : Option[Double],
                     askPrice : Option[Double]
                   )

package lokallykke.model.items

import java.sql.Timestamp

case class Item(
               id : Long,
               instagramId : Option[Long],
               bytes : Array[Byte],
               width : Option[Int],
               height : Option[Int],
               caption : Option[String],
               registered : Timestamp,
               costvalue : Option[Double],
               soldat : Option[Timestamp],
               soldfor : Option[Double],
               deletedAt : Option[Timestamp],
               askprice : Option[Double]
               )

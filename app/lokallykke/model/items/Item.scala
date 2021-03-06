package lokallykke.model.items

import java.sql.Timestamp

case class Item(
               id : Long,
               instagramId : Option[Long],
               bytes : Array[Byte],
               width : Option[Int],
               height : Option[Int],
               caption : Option[String],
               timestamp : Timestamp
               )

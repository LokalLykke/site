package lokallykke.instagram

import java.sql.Timestamp

case class InstagramItem(
                        id : String,
                        bytes : Array[Byte],
                        width : Int,
                        height : Int,
                        caption : Option[String],
                        timestamp: Timestamp,
                        filetype : String,
                        tags : Seq[String]
                        )

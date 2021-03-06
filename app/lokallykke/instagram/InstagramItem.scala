package lokallykke.instagram

import java.sql.Timestamp

case class InstagramItem(
                        id : Long,
                        bytes : Array[Byte],
                        width : Int,
                        height : Int,
                        caption : Option[String],
                        timestamp: Timestamp
                        )
